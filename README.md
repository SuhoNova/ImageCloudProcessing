# Image Processing on the Cloud
This repository contains two projects. An android application and a REST web service that can be deployed to a cloud server.
The goal of this project was to show the benefits of offloading a large computational tasks from a mobile phone to a cloud server. To investigate the benefits of offloading a computational task from a mobile phone to a cloud server, there are few things we have implemented. Firstly, we implemented an android application that can perform some sort of computational intensive tasks (i.e. image blurring using BoofCV library) locally. And then we implemented a web service deployed to a cloud server which the android application can offload the same task to.

# Android Application 
After updating the link to the web server inside file ___
Export the program as apk
Alternatively, you can use the provided APK to process your image using our web server.

# Web Service: Deploy to Azure Web Server (true of 26/05/2018)
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
