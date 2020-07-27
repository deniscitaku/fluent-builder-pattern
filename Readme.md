# Fluent Builder

Fluent Builder Pattern generator with Java Process Annotation

## Examples

Here we can see an example applying `@FluentBuilder` annotation on a simple java class.

![Alt text](screenshots/Screenshot%202020-07-27%20at%2022.03.47.png "Class annotated with @FluentBuilder")

you can see there that all we had to do is apply `@FluentBuilder` on a class level.

**Note:** the implementation of setter methods is required.

Then after compiling we can start using the Fluent Builder Pattern with the following name `PersonFluentBuilder`. See the example below:

![Alt text](screenshots/Screenshot%202020-07-27%20at%2022.05.13.png "Class annotated with @FluentBuilder")
![Alt text](screenshots/Screenshot%202020-07-27%20at%2022.05.35.png "Class annotated with @FluentBuilder")
![Alt text](screenshots/Screenshot%202020-07-27%20at%2022.06.35.png "Class annotated with @FluentBuilder")

Note that different from Builder Pattern, you cannot create the actual object without filling/assigning all values to all fields of the object. 
In this example we see that only `gender(char gender)` returns a `Person`. That's the beauty of Fluent Builder Pattern :) 
