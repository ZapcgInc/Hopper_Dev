# Hopper

Test suite currently builds and runs on java 8.


# Maven Build Options

To Run Test for a specific partner use "hopperPartner" option to pass partner config file name.

``
   mvn test -DhopperPartner=EPS
``

To Run specif API tests use cucumber tags option

``
   mvn test -Dcucumber.options="--tags @availability"
``

Combined way to support all parameter

``
mvn clean test -Dcucumber.options="--tags @availability" -DhopperPartner=EPS
``


#Known Issues

  1. Code has issues with JDK 9.
