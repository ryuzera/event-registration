package br.com.nlw.events.service;

import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicatorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repository.EventRepository;
import br.com.nlw.events.repository.SubscriptionRepository;
import br.com.nlw.events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {
        Event evt = eventRepository.findByPrettyName(eventName);
        if (evt == null) {
            throw new EventNotFoundException("Event: " + eventName + " not found");
        }

        User userRec = userRepository.findByEmail(user.getEmail());
        if(userRec == null) {
            userRec = userRepository.save(user);
        }

        User indicator = null;
        if (userId != null) {
            indicator = userRepository.findById(userId).get();
            if (indicator == null) {
                throw new UserIndicatorNotFoundException("User: " + userId + " not found");
            }
        }

        Subscription subs = new Subscription();
        subs.setEvent(evt);
        subs.setSubscriber(userRec);
        subs.setIndication(indicator);

        Subscription tmpSub = subscriptionRepository.findByEventAndSubscriber(evt, userRec);
        if (tmpSub != null) {
            throw new SubscriptionConflictException("Subscription already exists to user: " + userRec.getUserName() + " in event " + evt.getTitle()) ;
        }

        Subscription res = subscriptionRepository.save(subs);
        return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecraft.com/" + evt.getPrettyName() + "/" + res.getSubscriber().getId());
    }
}
