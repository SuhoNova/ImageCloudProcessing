# Image Processing on the Cloud
This repository contains two projects. An android application and a REST web service that can be deployed to a cloud server.
The goal of this project was to show the benefits of offloading a large computational tasks from a mobile phone to a cloud server. To investigate the benefits of offloading a computational task from a mobile phone to a cloud server, there are few things we have implemented. Firstly, we implemented an android application that can perform some sort of computational intensive tasks (i.e. image blurring using BoofCV library) locally. And then we implemented a web service deployed to a cloud server which the android application can offload the same task to.
Welcome to the ImageCloudProcessing wiki!

### Deploying web app to Azure
For the image processing Web Service, we deployed to Azure web server. 
To do this, firstly create an API App service on azure. And set the configuration such as Link, Location of web server, etc.
Go to "Application Settings" and set "Java Version" as Java 8 and "Java Web Container" as "Tomcat (9.0.0)".
If you are planning on deploying WAR file to the web server through FTP, go to "deployment credential section" and set your credentials.
The settings required for this web server has now been set. Now lets go deploy it!

There are multiple ways to deploy WAR file to the web server.
One way I chose to do it is through FTP with powershell using the guide provided by Microsoft [Deploy your app to Azure App Service with a ZIP or WAR file](https://docs.microsoft.com/en-us/azure/app-service/app-service-deploy-zip#deploy-war-file)

```
#PowerShell
$username = "<deployment_user>"
$password = "<deployment_password>"
$filePath = "<zip_file_path>"
$apiUrl = "https://<app_name>.scm.azurewebsites.net/api/zipdeploy"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $username, $password)))
$userAgent = "powershell/1.0"
Invoke-RestMethod -Uri $apiUrl -Headers @{Authorization=("Basic {0}" -f $base64AuthInfo)} -UserAgent $userAgent -Method POST -InFile $filePath -ContentType "multipart/form-data"
```

### Using the Android application
To install the app on your phone:
1. Ensure that your device is an Android phone running Android 5.0 (API 21) or above.
2. Get the prof-image.apk from the root of this repository.
3. Run the installer file in step 2 using a file manager on your phone.
4. Approve necessary permissions to allow the application to run.
5. Make sure that you are connected to the internet to use the remote processing feature of this application.

To run the application in Android Studio:
1. Import the project 'ImageProcessor' into Android Studio by: click 'File' -> 'Open' and select the ImageProcessor folder.
2. Run the application by clicking 'Run' > 'Run App'.


### Contributors
* Yao Jian Yap (RemRinRamChi) - yyap601@aucklanduni.ac.nz
* Hang-Chi Chuk (hchuk) - hchu167@aucklanduni.ac.nz
* Alex Yoo (SuhoNova) - syu680@aucklanduni.ac.nz
