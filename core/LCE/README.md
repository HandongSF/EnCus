# LCE Candidate Selector

## Overview
The `LCE` is a Java application designed to select the top candidate patches with high Longest Common Subsequence (LCS) scores. It operates by analyzing the difference between the Before Bug Introduction Commit (BBIC) and the Bug Inducing Commit (BIC) using the GumTree diff algorithm. The tool is designed to help researchers and developers identify similar patches from a predefined pool.

## Features
- Extracts and processes patches from a pool of candidate patches.
- Computes LCS scores to rank patch candidates.
- Uses Git repositories to retrieve relevant source code.
- Supports configurable extraction and similarity computations.
- Provides logging for debugging and monitoring execution.

## Requirements
- Java 8 or later
- Gradle

For additional details, refer to [SPI_Helper](https://github.com/ISEL-HGU/SPI_Helper) for pool generation.

