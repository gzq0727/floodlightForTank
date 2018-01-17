package tank.sdnos.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * just for test anotation
 * @author root
 *
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.METHOD) 
public @interface SDNTest {
	public boolean needClear() default true;
}
