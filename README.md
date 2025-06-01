what I did: 

1-Task: Calendar Conflict Optimizer.
2-keycloak integration.
3-keycloak realm json file for create realm, client and users.
4- spring boot job to sync keycloak users in calender database.
5-SQL + Query Optimization.
6-API Design + Implementation.
7- System Design Document simple digram showing application compnents (under /diagram).
8- DB migration using liquibase.
9- Dockerization : dockrize the (postgres, realm import, keycloak, calender service and added .env for config injection).

step to get it run:

1- install docker desktop and maven
2- clean an install the project to create jar
3- use docker compose to buid and run the project using docker-compose up
4-I created
 realm with name : booking-realm
client with name : booking-app 
three users
(username, password )  pairs below
admin user ("admin","admin")
owner user ("anwar","anwar")
normal user ("mohamed","mohamed")

json fie with users created in project /keycloak-setup/realm-json.json

docker compose will create above data using realm json file once you do docker-compose up

5- there is async schedule job (run every 3 minutes) to get users from keycloak and save in calender database
6- calender port is 8081  keycloak port is 8080
7- post-man collection API's you can find under /postman-collection
 - get the token using token API
 - you can use /api/bookings/validate to validate task1 (pass any token)
- then create propert(owner token)
- then use the proprt created booking (user token)
- you can use other methods to get proprties or bookings
