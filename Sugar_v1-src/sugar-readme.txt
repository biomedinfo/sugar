===============================================
=                                             =
= SUGAR (subtile-based GUI-assisted refiner)  =
= Source Code Repository                      =
=                                             =
= http://nagasakilab.csml.org/                =
=                                             =
= December 2013                               =
=                                             =
===============================================

The SUGAR is a GUI-based software to perform a detailed quality evaluation and cleaning of the Illumina 
high throughput sequencing data. It generates heatmap plots and line graphs of base-calling quality value (QV), 
read density, and mapping quality (MapQ; optional) score of sequence reads considering structures of 
the Illumina flowcells. Based on the results of the quality assessments by the SUGAR, 
the users can conduct both manual and automated data cleaning using GUI-based operations.

Current version of the SUGAR supports flowcells of the HiSeq2500 High Output mode (HCS 2.0.05), 
Rapid Run mode (HCS 2.0.x), and MiSeq v2 (Flow Cell v2). The SUGAR is developed initially 
as an extended version of the open source quality-control software FastQC (URL: http://www.bioinformatics.babraham.ac.uk/projects/fastqc/), 
and is freely available under GNU GPL v3 license at the URL http://nagasakilab.csml.org/.

I. Project Directory Structure 
The project directory structure is derrived from the FastQC source code structure.
The root directory (SUGAR_ROOT) includes the original FASTQC source code and the following added directories:
- org - SUGAR source code 
- SUGARHelp - end user documentation

II. Software Requirements
Java version 6 or later is required to build and run SUGAR.
ant version 1.8 or later is required to build SUGAR.

III. How-to-build
a. Build SUGAR application using ant scripts
- go to the root folder
- type "ant" - to build the SUGAR application
The Sugar.jar is created in the root folder
- type "ant -p" - to see other useful ant targets

b. Build using Eclipse
- start Eclipse workspace
- File | Import | Existing Project Into Workspace
- In the File Chooser select the SUGAR_ROOT directory
- Select and Import the projects found inside the root directory

IV. How-to-start
a. using a shell command
start GUI: java -jar Sugar.jar [options]
run Console: java -jar Sugar.jar [options] seqfile1 seqfile2 ... seqfileN
b. using Eclipse
- The SUGAR is started using the main class: org.csml.tommo.sugar.SugarApplication
- Specify the main class in Eclipse Run/Debug configuration
- Choose Run | Run from the menu to start this configuration

V. Citation
Yukuto Sato, Kaname Kojima, Naoki Nariai, Yumi Yamaguchi-Kabata, Mamoru Takahashi, Takahiro Mimori and Masao Nagasaki.
SUGAR: high-resolution refinement of high-throughput sequencing reads considering their spatial organization in flowcells.
Submitted.
