# Quarkus java 25 config bug

## To reproduce the issue:

1. run the project with `gradle quarkusDev`
2. connect using a ssh client to the port `2222`: `ssh -p 2222 localhost` then input any password
3. An error should be displayed on the quarkus logs, because the configuration property cannot be loaded

Switching back to java 21 in the gradle build fixes the issue
