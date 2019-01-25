package com.upgrade.campsite.repository;

import com.upgrade.campsite.presenter.BookingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class ReservationCacheRepositoryImpl implements ReservationCacheRepository {

    private static final String KEY = "reservations";

    @Autowired
    private RedisTemplate<String, LocalDate> template;

    @Override
    @Transactional
    public void addReservation(final LocalDate... dates) {
        template.opsForSet().add(KEY, dates);
    }

    @Override
    @Transactional
    public void removeReservation(final LocalDate... dates) {
        template.opsForSet().remove(KEY, dates);
    }

    @Override
    public Set<LocalDate> getReservations() {
        return template.opsForSet().members(KEY);
    }

    @Override
    @Transactional
    public void initializeCache(LocalDate... dates) {
        template.delete(KEY);
        addReservation(dates);
        template.expire(KEY, 1, TimeUnit.DAYS);
    }

    @Override
    @Transactional
    public Boolean isCacheInitialized() {
        Boolean b = template.hasKey(KEY);
        if (b == null) {
            template.hasKey(KEY);
            System.out.println(template.getConnectionFactory().getConnection().keys(KEY.getBytes()));

            template.afterPropertiesSet();
            System.out.println(template.getConnectionFactory().getConnection().ping());
            System.out.println(template.getConnectionFactory().getConnection().isClosed());
            System.out.println(template.keys("*"));
            template.opsForSet().add(KEY, LocalDate.now());
            System.out.println(template.opsForSet().members(KEY));
            template.opsForList().leftPush("TESTE", LocalDate.now());
            System.out.println("template.opsForValue().get() = " + template.opsForList().size("TESTE"));
        }
        return b;
    }

}
