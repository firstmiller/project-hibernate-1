package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.HBM2DDL_AUTO, "update");

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        int offset = pageNumber < 0 ? 0 : pageNumber * pageSize;
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> nativeQuery = session.createNativeQuery(
                    "SELECT * FROM player LIMIT :limit OFFSET :offset", Player.class);
            nativeQuery.setParameter("limit", pageSize);
            nativeQuery.setParameter("offset", offset);
            return nativeQuery.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()) {
            Query<Long> namedQuery = session.createNamedQuery("Player_GetCountPlayers", Long.class);
            return namedQuery.getSingleResult().intValue();
        }
    }

    @Override
    public Player save(Player player) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
            return player;
        }
        catch (Exception e) {
            if(transaction != null)
                transaction.rollback();
            throw e;
        }
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Player player1 = (Player) session.merge(player);
            transaction.commit();
            return player1;
        }
        catch (Exception e) {
            if(transaction != null)
                transaction.rollback();
            throw e;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            Player player = session.get(Player.class, id);
            return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(player);
            transaction.commit();
        }
        catch (Exception e) {
            if(transaction != null)
                transaction.rollback();
            throw e;
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}