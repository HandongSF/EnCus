# ChangeCollector

## Overview
ChangeCollector is a Java-based tool designed to extract change vectors from source code repositories or Defects4J bugs. It provides functionality to collect change vectors between the current commit and the previous one for a single source code file or a Defects4J bug.

## Features
- Extracts source code differences between commits in a GitHub repository
- Collects commit information and stores it in CSV format
- Extracts change vectors using GumTree
- Supports Defects4J bug tracking for analyzing changes in known buggy code
- Configurable through properties files
- Logging support using Log4j

## Prerequisites
- Java 8 or later
- Gradle
- Defects4J (for defect tracking mode)
