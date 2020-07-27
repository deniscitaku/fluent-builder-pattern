import annotation.FluentBuilder;

import java.time.LocalDate;

@FluentBuilder
public class Person {

  private String name;

  private LocalDate birthDate;

  private char gender;

  // Getters and Setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public char getGender() {
    return gender;
  }

  public void setGender(char gender) {
    this.gender = gender;
  }
}
