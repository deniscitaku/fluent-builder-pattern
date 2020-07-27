import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

/**
 * Created on: 7/27/20
 *
 * @author Denis Citaku
 **/
public class PersonFluentBuilderTest {

  @Test
  public void test() {
    Person person = PersonFluentBuilder.builder()
        .name("Denis")
        .birthDate(LocalDate.of(1998, Month.MARCH, 1))
        .gender('M');

    assertEquals("Denis", person.getName());
    assertEquals(LocalDate.of(1998, Month.MARCH, 1), person.getBirthDate());
    assertEquals('M', person.getGender());
  }
}
