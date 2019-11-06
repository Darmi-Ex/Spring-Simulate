import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;

public class Test {
    public String whenChaining_thenOk() {
        User user = new User("anna@gmail.com", "1234");
        Optional<User> result = Optional.ofNullable(user);
        final String aDefault = result.map(User::getIsCode)
                .map(Objects::toString)
                .orElse("default");
        return aDefault;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        System.out.println(System.currentTimeMillis() );

        User user = new User("anna@gmail.com", "1234");
        final Class<? extends User> aClass = user.getClass();
        final Field email = aClass.getDeclaredField("email");
        email.setAccessible(true);
        final String o = (String) email.get(user);
        System.out.println(o);

        Comparable<Object> objectComparable = new Comparable<Object>() {
            @Override
            public int compareTo(@NotNull Object o) {
                return 0;
            }
        };
    }
}
