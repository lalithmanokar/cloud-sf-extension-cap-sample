# SucessFactors Extension Application - "Run Smooth"

## Description: 
`Run Smooth` is a reference application that shows how to extend SuccessFactors employee central with an application that runs on SAP Cloud Platform. 

This application showcases:
1. Using SAP Cloud Platform Extension Factory service
2. Cloud Application Programming Model(CAP) to build application
3. Handling of SF events via Enterprise Messaging(Webhooks)
4. Connectivity with SF REST API’s
5. SCI(IAS) Tenant integration with SF


#### Scenario: 

Managers can maintain the details of their direct reports and the projects that they are working on in `Run Smooth` application. 
When an employee decides to leave the team/ company, an event is triggered in the SuccessFactors system. The Run Smooth application subscribes to this event and sends out a notification to the manager with the topics to be handed over to fellow team mates and the preferred skill set for the replacement hire to maintain the status quo in the team. 

#### Features:
* Login with SuccessFactors user Id, password. 
* View the list of projects, employees working on the projects. 
* Get notification when an employee is leaving the team with the consolidated report on the skills of the employee.

## Architecture

### Solution Diagram

![solution diagram](./documentation/images/SolutionDiagram.PNG)

The Run Smooth application is developed using [SAP Cloud Application programming Model (CAP)](https://cap.cloud.sap/docs/) and runs on Cloud Foundry Environment. It consumes platform services like Enterprise Messaging, HANA and Connectivity. The events generated in SuccessFactors are inserted into the Enterprise messaging queue. The application running in Cloud Foundry polls the queue for these messages and inserts them into the HANA database. The run smooth application also makes calls to SF oData APIs to get SF data. 

## Requirements 
* [Node js](https://nodejs.org/en/download/)
* SuccessFactors test/demo instance. 
>Note: Please do not try this application on a productive instance. 
* [Cloud Foundry Command Line Interface (CLI)](https://github.com/cloudfoundry/cli#downloads)
* Cloud Foundry trial or enterprise account, [sign up for a Cloud Foundry environment trial account on SAP Cloud Platform](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/76e79d62fa0149d5aa7b0698c9a33687.html)
* To deploy the MTAR we need the MTA CF CLI plugin, download the MTA CF CLI Plugin from [here](https://tools.hana.ondemand.com/#cloud)
* The MultiApps CF CLI Plugin is now also available on the CF Community Repository. To install the latest available version of the MultiApps CLI Plugin execute the following:

cf install-plugin multiapps

* If you do not have the community repository in your CF CLI you can add it first by executing:

cf add-plugin-repo CF-Community https://plugins.cloudfoundry.org

* The multi-target application archive builder is a standalone command-line tool that builds a deployment-ready multi-target application (MTA) archive .mtar file from the artifacts of an MTA project according to the project’s MTA development descriptor (mta.yaml file).The archive builder is used on a file system independently of the development environment in which the application project has been created. The build process and the resulting MTA archive depend on the target platform on which the archive is deployed. Download MTA archive builder - jar file from [here](https://tools.hana.ondemand.com/#cloud) and rename it as mta.jar


## Configuration

### Step 1: Configure trust between SF and CP using Extension Factory
 
[Refer the document to set up trust, SF destination using Extension Factory.](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/9e33934540c44681817567d6072effb2.html) Follow steps 1,2,3 in the document. 
In Step 3 of the document, while creating Extension Fatory service instance, provide `sfextension-service` as the name of the instance. 

### Step 2: Deploy the reference application

1. [Clone](https://help.github.com/articles/cloning-a-repository/) this repository
2. Edit mta.yaml file. 
   set the systemName as the name of the SF system that you registered in previous step.
2. Build the application
    `mbt build -p = cf `  
3. Login to Cloud Foundry by typing the below commands on command prompt
    ```
    cf api <api>
    cf login -u <username> -p <password> 
    ```
    `api` - [URL of the Cloud Foundry landscape](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html) that you are trying to connect to.
    
    `username` - Email address of your sap.com account.
    `password` - Your sap.com password
    
    Select the org and space when prompted to. For more information on the same refer [link](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/75125ef1e60e490e91eb58fe48c0f9e7.html#loio4ef907afb1254e8286882a2bdef0edf4).

4. Deploy the application
	
	Navigate to mta_archives folder.
	
   `cf deploy cloud-cap-xf-sf_0.0.1.mtar`


### Step 3: Setting up SuccessFactors system

In this step, you will configure the successFactors system to send message to the Enterprise Messaging service on Cloud platform. 

1. Login to the sf demo instance. 
 
    username: sfadmin
	password: It will be provided to you in the mail that you receive on requesting for demo instance. 
	
2. Setting Outbound OAuth Configurations. In this step, the credentials required to send messages to the Enterprise Messaging service are set. 
   
   1. In the demo instance search bar, search for `Integration center`
   2. Select `Security center`
   3. Select `Outbound OAuth Configurations`
   4. Click on `add` to create new OAuth configuration
   5. Enter the below detail:
  
      OAuth Type: `OAuth 2.0`
  
      Grant Type: `Client_Credentials`
  
      `Client ID`, `Client Secret`, `Token URL`: you can get these details from the service key of the enterprise message service instance you created in Step 1. 
  
      Token URL: append the token url with `/oauth/token`
  
      Add Custom Header Parameters. 
 
      Add new row: key=x-qos. Value =1 
      
3. Creating integration. An integration specifies the endpoint to which a message sgould be send along with the content of the message.
   1. Navigate to integration center. 
   2. Select `My integrations`
   3. Select `create` > `More integration types`
   4. Select the below details and click on create:

      Trigger type: intelligent service
 
      Destination type: REST
 
      Format: json
  
   ![Integration type](./documentation/images/integration.PNG)

4. Search for the [intelligent service](https://help.sap.com/viewer/b8da92fb08c347cbab0c8fff82af0a3f/1911/en-US/3d91617db9a94903a0237747e4eda11f.html) `Employment Termination`. Select it and click on `Select` button.

5. Provide details for the integration. 
   1. Enter name for the integration and click next
   2. In Configure Fields tab, Click `+` button . Insert sibling elements
   3. Give label as 'userid'
   4. Click on `set an Assosiated field` button. Select entity tree view. Select `User ID`. Click on 'Change Association to user id'
   5. Similarly add new sibling for managerId. and associate it with supervisor id. 
   6. Add new sibling element `message` with default value as 'resigned'
   7. Keep the default settings for `Response field`, `filter` tabs. 
   8. Edit the `Destination Settings` with the following details:
      
      REST API URL: give the URL of the queue
      `https://enterprise-messaging-pubsub.cfapps.eu10.hana.ondemand.com/messagingrest/v1/topics/< topic name >/messages`
 
      Authentication type: OAuth
 
      OAuth Configuration: Select the configuration created in Step 2.
 
      ![Destination settings](./documentation/images/DestinationSettings.PNG)
  
   9. Click on `save`
 
   10. In `Review and Run` tab - click on `run now`
 
 6. Configuring the event flow. This step ensures that when the event is triggered, the integration created in the above step is run. 
   1. Search for `intelligent service` in the demo instance search bar. 
   2. Select `Employment termination` event. There are many more events available, in our scenario, 
   3. Add integration for the existing flow: 
      - click on `Integration` under `Activities`. (on the right-hand side corner)
      - Select the integration created in the previous step. click on `Add integration`
 
### Step 4: Setup your own IAS tenant for authentication [Optional]

1. Request [IAS tenant ID](https://tenants.ias.only.sap/)
2. Follow [SAP CF subaccount trust configuration](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/7c6aa87459764b179aeccadccd4f91f3.html#loioaedb8eed952b41c4b87c50b92bf651e4)
3. Follow the steps under “Procedure to setup trust between IAS and BizX tenant” in the [document](https://confluence.successfactors.com/pages/viewpage.action?pageId=255365887) to set up SSO. 
4. Add metadata in tenant.
5. Add the required SF user, sfadmin in you tenant. Click on 'Add User' in <tenant URl>/admin/#/users
   Enter the details of the user. Set the initial password for the user from Authentication tab-> password details -> set initial password. 
6. From the applications tab of your tenant, set the 'Subject Name Identifier' as 'Employee number' for your application and as 'Login Name' for your SF instance. 

## Demo Script

1. Login to SF demo instance with sfadmin user.
![step1](./documentation/images/step1.PNG)
2. Search for Employee David Leal (dleal) in the Employee Directory
3. Select Employee David Leal
4. Click on Actions button and Select Org Chart
![step4](./documentation/images/step4.PNG)
5. Choose an employee who is a direct report of 'David Leal e.g Penelope Miller(pmiller)
6. Click on Take Actions button and Select Termination
![step6](./documentation/images/step6.PNG)
7. Set values for 
      - Termination Date
      - Termination Reason - Early Retirement
      - Ok to Rehire - Yes
      - Regret Termination - Yes
![step7](./documentation/images/step7.PNG)
8. Click on Save.
9. In the please confirm your request. Click on the 'Show workflow participants' link. 
10. Workflow participants would be shown as Paul Atkins (Production Director), Tessa Walker (HR Business Partner Global)
11. Click on Confirm button
12. Use Proxy Now functionality and proxy login as Paul Atkins(patkins)
![step12](./documentation/images/step12.PNG)
13. In the Home page of Paul Atkins click on tile Approve Requests
14. Click on Approve button for the request for approval of Early Retirement of Penelope Miller
![step14](./documentation/images/step14.PNG)
15. Use Proxy Now functonality and proxy login as Tessa Walker(twalker)
16. In the Home page of Tessa Walker click on tile Approve Requests
17. Click on Approve button for the request for approval of Early Retirement of Penelope Miller
18. Open the Web Application UI for Run Smooth application in browser. 
19. Login with email address of David Leal(david.leal@bestrunsap.com). 
20. Click on notifications tile. 
21. Notification will be displayed regarding Resignation of Penelope Miller along with her Skillset. 

## Known Issues

No known issues.

## How to Obtain Support

In case you find a bug, or you need additional support, please open an [issue](https://github.wdf.sap.corp/staging-for-SAP-samples-public/cloud-sf-extension-cap-sample/issues) here in GitHub.

## To-Do (upcoming changes)

None

## License

Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved. This file is licensed under SAP Sample Code License Agreement, except as noted otherwise in the [LICENSE](/LICENSE) file.
