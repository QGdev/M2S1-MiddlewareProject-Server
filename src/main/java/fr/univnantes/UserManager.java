package fr.univnantes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class used to manage users
 * <p>
 *     This class is used to create, get and remove users
 *     It is a singleton
 * </p>
 */
public class UserManager {

    private final static AtomicReference<UserManager> instance = new AtomicReference<>(null);
    private final ConcurrentHashMap<String, User> users;


    /**
     * Creates a new user manager
     */
    private UserManager() {
        users = new ConcurrentHashMap<>();
    }

    /**
     * Returns the instance of the user manager
     * Creates it if it does not exist
     *
     * @return  The instance of the user manager
     */
    public static UserManager getInstance() {
        if (instance.get() == null) {
            synchronized (UserManager.class) {
                instance.compareAndSet(null, new UserManager());
            }
        }
        return instance.get();
    }

    /**
     * Returns the user with the given UUID
     *
     * @param userId    The UUID of the user
     * @return          The user with the given UUID
     */
    public User getUser(String userId) {
        return users.get(userId);
    }

    /**
     * Creates a new user with the given name
     *
     * @param name  The name of the user
     * @return      The created user
     */
    public User createUser(String name) {
        User user = new User(name);
        users.put(user.getUUID(), user);
        return user;
    }

    /**
     * Removes the user with the given UUID
     *
     * @param userId    The UUID of the user
     * @return          True if the user has been removed, false otherwise
     */
    public boolean removeUser(String userId) {
        return users.remove(userId) != null;
    }
}
