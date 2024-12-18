# EnCus: Ensemble-based Search Space Customization for Automated Program Repair

---

[EnCus Result - Google Spreadsheet](https://docs.google.com/spreadsheets/d/e/2PACX-1vR_H_gs_0kyAa0ioX9FeJehpIDRqY-bWyxvCoBHz49H1pubUlNfiOGQQSviQI_6idhs8KD7JovzUDZt/pubhtml)


[EnCus Result - Google Spreadsheet](https://docs.google.com/spreadsheets/d/e/2PACX-1vR_H_gs_0kyAa0ioX9FeJehpIDRqY-bWyxvCoBHz49H1pubUlNfiOGQQSviQI_6idhs8KD7JovzUDZt/pubhtml)

[EnCus Installation](https://docs.google.com/document/d/e/2PACX-1vSoYRt1hmdn_ejrmxwdcRJULARg_wmuS3MO6L0QRDrU0SmIas9hbE-rLUhKdvMGMOzOe-5coaqfHmMB/pub)

---

## Submodules (Projects included)
**ENCUS = CC + LCE + APR(ConFix)**
- [ConFix](https://github.com/thwak/confix) (Original Repository of ConFix)
- [ChangeCollector (CC)](https://github.com/ISEL-HGU/ChangeCollector)
- [Longest Common sub-vector Extractor (LCE)](https://github.com/S0rrow/LCE)

---

## How to run

### Requirements
- JDK**s**
    - Oracle JDK 17 (Used to compile `ChangeCollector` and `Longest Common sub-vector Extractor`)
    - Oracle JDK 8 (Used by Defects4J)
    - Oracle JDK 7 (Used by SimFix)
- [Defects4J](https://github.com/rjust/defects4j)
    - See [requirements](https://github.com/rjust/defects4j#requirements)
- Python
    - Python 3.6+
    - PIP packages necessary
        - jproperties

### Pre-run Configuration
- Launch `preconfig.sh` (for linux) to install pre-requisites. Those will be installed **locally**:
    - [cpanminus](https://metacpan.org/pod/App::cpanminus)
    - [Defects4J](https://github.com/rjust/defects4j)
    - [SDKMAN!](https://sdkman.io/)
        - JDK 17 : Used to compile `ChangeCollector` and `Longest Common sub-vector Extractor`
        - JDK 8 : Used by Defects4J Framework
        - JDK 8 : Used by SimFix
        - Maven : Used by ConFix
        - Gradle : Used by `ChangeCollector`,`Longest Common sub-vector Extractor` and Defects4J Framework

### How to generate the Change Vector Pool
- `Change Collector` is a submodule of `SPI` which is responsible for generating the change vector pool.
    - `Change Collector` is a Java project which is built with Gradle.
- To execute the `ChangeCollector` module, you need to have JDK 17 installed.
- To launch `ChangeCollector` to generate the change vector pool, input values descripted below in target properties file.
    - ChangeCollector itself does not use `SPI.ini` file. Instead, it uses `.properties` file for input.
- The path of `.properties` file can be given by argument. If not, `ChangeCollector` will look for `cc.properties` under `{Path_to_SimilarPatchIdentifier}/core/ChangeCollector` directory.
- The `.properties` file should contain the following values to generate pool.
    - `project_root` : Absolute path to the root directory of the project.
    - `output_dir` : Absolute path to the directory where the output files should be stored.
    - `mode` : For Change Vector Pool generation, the mode should be `poolminer`.
    - `JAVA_HOME.8` : Absolute path to JDK 8.
    - `set_file_path` : Absolute path to the file which contains the list of metadata; {BIC commit ID, BFC commit ID, path for BIC file, path for BFC file, Git URL, JIRA key}.
- Execute the following command to launch `ChangeCollector` to generate the change vector pool.
   - If you want to execute `ChangeCollector` with designated properties file, provide path as additional argument.
> `cd ./core/ChangeCollector {Path of .properties file}`<br>
> `gradle clean run`

### How to launch
1. Change value at key `mode` in section `EnCus` among [`defects4j`, `defects4j-batch`, `github`].
2. Edit `SPI.ini`:
    - [Keys to change for all modes](README.md#things-to-modify-at-spiini-in-all-modes)
    - [Keys to change for mode `defects4j`](README.md#things-to-modify-at-spiini-additionally-with-mode-defects4j)
    - [Keys to change for mode `defects4j-batch`](README.md#things-to-modify-at-spiini-additionally-with-mode-defects4j-batch)
    - [Keys to change for mode `github`](README.md#things-to-modify-at-spiini-additionally-with-mode-github)
3. You can run SPI through command below at project root directory; however at first launch or after submodule changes, you need to rebuild submodules; Add option `-r` / `--rebuild` to do so.
> `python3.6 launcher.py`
4. If finished, the result of the execution will be stored within the folder inside the path set by key `byproduct_path`.
    - If "diff.txt" is found within this path, it means the patch is found.
    - Otherwise it means that there is no patch found for the buggy file.

#### Things to modify at `SPI.ini` in all modes
|**section**|**key**|**description**|**default value**|
|:---|:---|:---|:---|
|`ENCUS`|`mode`|How `ENCUS` will be run. Can choose among those options:<br>- `defects4j` : Tells `ENCUS` to try finding a patch out of a `Defects4J` bug.<br>- `defects4j-batch` : Tells `ENCUS` to try finding a patch out of a `Defects4J` bug, but with a number of bugs given as a list.<br>- `github` : *Currently not fully implemented.* Tells `EnCus` to try finding a patch out of a `GitHub` project with a bug.|-
|`EnCus`|`JAVA_HOME_8`|Absolute path of JDK 8|None, *Should be specified*|
|`EnCus`|`JAVA_HOME_7`|Absolute path of JDK 7|None, *Should be specified*|
|`EnCus`|`byproduct_path`|Directory which files and folders made during the progress of `EnCus` should be stored into.|`{EnCus_root_directory}/byproducts`|
|`EnCus`|`root`|Directory where `EnCus` root directory is placed.|.|
|`EnCus`|`patch_strategy`|List of patch strategies (among `flfreq`, `tested-first`, `noctx`, `patch`) to run `EnCus` with. Comma-separated.|`flfreq`|
|`EnCus`|`concretization_strategy`|List of concretization strategies (among `tcvfl`, `hash-match`, `neightbor`, `tc`) to run `EnCus` with. Comma-separated.|`hash-match`|
|`ConFix`|`patch.count`|Number of patch generation trials|`200000`|
|`ConFix`|`max.change.count`|The threshold of number of changes to use as patch material|`2500`|
|`ConFix`|`max.trials`|The threshold of patch generation trial|`100`
|`ConFix`|`time.budget`|Time limit of ConFix execution, hourly|`3`|
|`ConFix`|`fl.metric`|How Fault Localization(FL) is done|`perfect`|

#### Things to modify at `SPI.ini` additionally with mode `defects4j`
|**section**|**key**|**description**|**default value**|
|:---|:---|:---|:---|
|`EnCus`|`identifier`|Alias to the name of the project.|None|
|`EnCus`|`version`|Bug ID of Defects4J bug.|None|

#### Things to modify at `SPI.ini` additionally with mode `defects4j-batch`
|**section**|**key**|**description**|**default value**|
|:---|:---|:---|:---|
|`EnCus`|`batch_d4j_file`|Name of the file which contains names of Defects4J bugs|`d4j-batch.txt`|

#### Arguments
- `'-r', '--rebuild'` : Rebuild all submodules (ChangeCollector, LCE) on start of execution. In default, `launcher.py` does not rebuild each submodules on execution.
- `'-d', '--debug'` : Execute single Defects4J project, `Closure-14` for testing a cycle of execution. Debug uses `flfreq` and `hash-match` strategies. EnCus consists of three Java projects as submodules. Thus you may need to check if there is no compile error or wrong paths given through debug execution. If no problem occurs, you are clear to launch.
- `'-A', '--APR'` : Choose APR tool between `ConFix` and `SimFix`. Default APR tool is `ConFix`.
- `'-p', '--pool'` : Optional, you can provide patches directly being used for ConFix. Patches should be a form of #_new.java. The number should be consecutive.
- `'-t', '--textsim'` : Use Cosine Text Similarity to extract patches. It extracts 3 * candidate numbers using LCE and top candidate numbers using cosine similarity. Giving it the value `tree`, the text similarity is evaluated based on the result of the code differencing.
- `'-D', '--Differencing'` : Choose the code differencing tool to be used during the `ChangeCollector` stage. Options include: `GumTree3.0`, `Gumtree4.0`, and `LAS`.
- `'-S', '--Dataset'` : Choose the pool dataset EnCus searches. Options include: `Starred`, `GBR`, and `Type`. `Starred` is based on the most starred Apache Java projects. `GBR` is based on an open source bug repository, [GrowingBugRepository](https://github.com/liuhuigmail/GrowingBugRepository). `Type` chooses projects that are of similar 'type', from [Awesome Java](https://java.libhunt.com/).

### Upon Execution...
#### "Notify me by Email"
- You may use inserted bash script, `tracker.sh` for notifying execution finish through email. Through bash script, `tracker.sh` will execute `launcher.py` with *rebuild* option given.
- You must use `handong.ac.kr` account only for email.
    - due to Gmail Rules, we cannot use *gmail* accounts for mailing within SERVER #24.
#### How to use *tracker.sh*
> `./tracker.sh` `{location_of_EnCus}` `{your@email}`

#### `SPI.ini` Specified Description
##### **ENCUS**
|key|is_required|description|
|:---|:---:|:---|
|`mode`|Yes|How `ENCUS` will be run. Can choose among those options:<br>- `defects4j` : Tells `ENCUS` to try finding a patch out of a `Defects4J` bug.<br>- `defects4j-batch` : Tells `ENCUS` to try finding a patch out of a `Defects4J` bug, but with a number of bugs given as a list.<br>- `github` : *Currently not fully implemented.* Tells `ENCUS` to try finding a patch out of a `GitHub` project with a bug.|
|`batch_d4j_file`|In mode `defects4j-batch`|Name of the file which contains names of Defects4J bugs|
|`identifier`|In mode `defects4j`|Alias to the name of the project.<br>*Automatically set when running `ENCUS` in mode `defects4j-batch`*|
|`version`|In mode `defects4j`|Bug ID of Defects4J bug.|
|`repository_url`|In mode `github`|URL of GitHub project to look for a patch upon|
|`commit_id`|In mode `github`|Commit ID of GitHub project that produces a bug|
|`source_path`|In mode `github`|Source directory path (relative path from project root) for parsing buggy codes|
|`target_path`|In mode `github`|Relative path (from project root) for compiled .class files|
|`test_list`|In mode `github`|List of names of test classes that a project uses|
|`test_class_path`|In mode `github`|Classpath for test execution. Colon(`:`)-separated.|
|`compile_class_path`|In mode `github`|Classpath for candidate compilation. Colon(`:`)-separated.|
|`build_tool`|In mode `github`|How a project is built. Only tools `maven` and `gradle` are available|
|`faulty_file`|In mode `github`|Relative directory (from root of project) of a faulty file. *Automatically set when running `ENCUS` in mode `defects4j` / `defects4j-batch`*|
|`faulty_line_fix`|In mode `github`|Line number of `faulty_file` to try modifying. *Automatically set when running `ENCUS` in mode `defects4j` / `defects4j-batch`*|
|`faulty_line_blame`|In mode `github`|Line number of `faulty file` where the bug is made. *Automatically set when running `ENCUS` in mode `defects4j` / `defects4j-batch`*|
|`JAVA_HOME_8`|**Yes**|Absolute path of JDK 8. Necessary to build and compile ConFix.|
|`JAVA_HOME_7`|**Yes**|Absolute path of JDK 7. Necessary to build and compile SimFix.|
|`byproduct_path`|No|Directory which files and folders made during the progress of `ENCUS` should be stored into. *Will make folder `byproducts` inside `root` by default.*|
|`root`|No|Directory where `ENCUS` root directory is placed.|
|`patch_strategy`|No|List of patch strategies (among `flfreq`, `tested-first`, `noctx`, `patch`) to run `ENCUS` with. Comma-separated.|
|`concretization_strategy`|No|List of concretization strategies (among `tcvfl`, `hash-match`, `neightbor`, `tc`) to run `ENCUS` with. Comma-separated.|
|`stored_pool_dir`|Yes|Path of git projects going to be used as pool

##### **Change Collector**
|**key**|**is_required**|**description**|
|:---|:---:|:---|
|`project_root`|no|*automatically set by launcher through `ENCUS`/`root`*|
|`output_dir`|no|*automatically set by launcher through `ENCUS`/`byproduct_path`*|
|`mode`|no|*automatically set by launcher*|
|`file_name`|no|*automatically  set by launcher through `ENCUS`/`target_path`*|
|`commit_id`|no|*automatically set by launcher through `ENCUS`/`commit_id`*|
|`git_url`|no|*automatically set by launcher through `ENCUS`/`repository_url`*|
|`git_name`|no|*unnecessary if `git_url` is given*|
|`doClean`|no|whether to clean the output directory before recurrent execution with identical output directory|
|`JAVA_HOME.8`|no|*automatically set by launcher through `ENCUS`/`JAVA_HOME_8`*|
|`defects4j_name`|no|*automatically set by launcher through `ENCUS`/`identifier`*|
|`defects4j_id`|no|*automatically set by launcher through `ENCUS`/`version`*|
|`hash_id`|no|*automatically set by launcher*|

##### **LCE**
|**key**|**is_required**|**description**|
|:---|:---:|:---|
|`ENCUS.dir`|no|*automatically set by launcher through `ENCUS`/`root`*|
|`pool_file.dir`|no|*automatically set by launcher*|
|`meta_pool_file.dir`|no|*automatically set by launcher*|
|`target_vector.dir`|no|*automatically set by launcher*|
|`pool.dir`|no|*automatically set by launcher*|
|`candidates.dir`|no|*automatically set by launcher*|
|`candidate_number`|**yes**|select the number of candidate source codes. default is 10|
|`d4j_project_name`|no|*automatically set by launcher through `ENCUS`/`identifier`*|
|`d4j_project_num`|no|*automatically set by launcher through `ENCUS`/`version`*|
|`doClean`|no|whether to clean the output directory before recurrent execution with identical output directory|
|`threshold`|no|eliminate the vector according to length of vector|
|`text_sim`|**yes**|automatically set by launcher|

##### **ConFix**
|**key**|**is_required**|**description**|
|:---|:---:|:---|
|`jvm`|yes|*automatically set by launcher through `ENCUS`/`JAVA_HOME_8`*|
|`version`|no|Version of JDK. *automatically set by launcher*|
|`pool.path`|no|*automatically set by launcher*|
|`cp.lib`|no|*automatically set by launcher*|
|`patch.count`|yes|define the patch generation trial count. default is 200000|
|`max.change.count`|yes|define the threshold of number of changes to use as patch material|
|`max.trials`|yes|define the threshold of patch generation trial|
|`time.budget`|yes|define the time limit of ConFix execution|
|`patch.strategy`|no|*automatically set by launcher*|
|`concretize.strategy`|no|*automatically set by launcher*|
|`fl.metric`|yes|define how Fault Localization is done. default is perfect. only required for ConFix|
