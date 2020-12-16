#!/bin/sh
java -cp /usr/share/java/postgresql-jdbc.jar:/tessera/tessera-app.jar:. com.quorum.tessera.launcher.Main -configfile /ptm/config.json
