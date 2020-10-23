# Contributing to the MBP IoT Platform
To contribute, please clone the project, create a new branch where you commit changes and afterwards create a pull request (PR) to get your changes revised and merged into the master branch.
The following commands can be executed in a command shell once a git client has been installed.

## Clone the project
Create a new directory with a working copy of the master branch

````bash
git clone https://github.com/IPVS-AS/MBP.git
````

## Checkout new branch
````bash
git checkout -b feature/<your-feature-name>
````

If changes are related to an existent issue, use:

````bash
git checkout -b issue/<issue_number>
````

## Commit and push your changes

If changes are related to an existent issue, link the issue number on the commit message.

````bash
git add <modified_files>
git commit -m '<message> #<issue_number_if_existent>'
git push --set-upstream origin <your-branche-name>
````