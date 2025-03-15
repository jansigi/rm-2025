# README

## Overview

- Competitor: *Jan Sigrist*
- Language: *Kotlin*
- Technology: *Kotlin Multiplatform, targeting Desktop*
- IDE: *IntelliJ*

## Database

This application uses either an external MySQL database or an in-memory H2 database.

MySQL database connection details can be found in the `/data/DatabaseFactory.kt` file.

The credentials are hardcoded and are:
- user: root
- password: 1234

## Execution

```bash
cd executable # go into the executable dir
``` 
```bash
java -jar composeApp-desktop-<version>.jar # or double click on jar
```
If the newest version doesn't work, please try another.


