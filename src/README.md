#How to run on the command line:

- Left-click on the build.gradle file and click Reload Projects
- In folder g11 and run:

Server:
```bash 
gradle build

gradle run --args="server"

#Before running another made server do:
gradle --stop
```

Client:

```bash 
gradle build

gradle run --args="client"
```

