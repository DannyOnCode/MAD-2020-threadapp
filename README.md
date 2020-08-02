# Thread
A social app made for an assignment.
Codeveloped by Eugene Long (S10193060J), Danny Chan (S10196363F) and Mohammad Thabith (S10196396B).

## description.

*An assignment for partial fulfillment of the coursework of Mobile Applications Development of AY2019/20*

Stay in touch with your friends and family with Thread, an all new free social app on Android!

Thread uses your phone’s internet connection and lets you text and share posts quickly and easily!

Switch to Thread and break free from your SMS charges!

LEVEL UP!
Aim to have the highest XP amongst your friends with our unique levelling system for each server.

CUSTOMISE EVERYTHING!
Set your own Name, Status and share more about yourself on the “About Me” section! 
Set up your own server with a Server name, Server description and Member Titles of your choice!

MULTIPLE ANDROID DEVICES?
Access your servers, messages and posts from all your android devices at once!  Even your settings are synced across devices!

LOW ON STORAGE?
Not to worry, all images and texts are stored on our server.

INSTANT NOTIFICATIONS!
Never miss out on new texts and posts with our instant notifications. You can customise what notifications you will like to recieve.

## documentation.

The documentation for this application was made using JavaDoc.
A generated copy can be found hosted [here](https://orbitalsqwib.github.io/MAD-2020-threadapp/).
(Done for Stage 2)

## technologies.

### firebase.

In this project, we decided to use Firebase for authentication, chat storage and sharing, file storage and notification transmission.

Specific Features Used:
- Authentication: Used for register/login, resetting forgotten passwords
- Realtime Database (w/ Rules): Used to store and secure all shared data used in the application
- Storage: Used to store all image data such as profile photos/posts
- Cloud Functions: Used to generate a server side timestamp for chat messages.
- Cloud Messaging: Used for sending notifications.

We also attempted using the Firebase Test Lab for testing, and was partially successful in getting it to test the functions of the application.
However, due to the limitations of a free plan and technical issues with the crawler, we did not manage to fully test the entire application.

#### database structure.

```json
"thread" : {
    "users" : {
          "userId" : {
              "statusMessage" : "status message here",
              "profileImageURL" : "profile image URL here",
              "_notifications" : {
                          "_msg" : "on/off status",
                          "_post" : "on/off status",
                          "_system" : "on/off status"   
                        },
              "aboutMeMessage" : "description here",
              "_token" : "tokenId",
              "subscribed_servers" : ["server ids here"]
            }
        },
    "servers" : {
          "serverId" : {
                "name" : "server name here",
                "owner" : "owner id here",
                "description" : "server description here"
             }
        },
    "shares" : {
          "shareCode" : "server code here"
        },
    "members" : {
          "serverId" : {
                 "memberId" : "user exp"
              }
        },
    "messages" : {
           "serverId" : {
                  "messageId" : {
                        "sender" : "sender name here",
                        "senderUID" : "sender uid here",
                        "messageText" : "text message here",
                        "timestamp" : "time messaged was sent here"
                     }
              }
        },
    "postmessages" : {
           "serverId" : {
                  "postId" : {
                        "commentId" : {
                                "_comment" : "comment message here",
                                "_senderUID" : "sender uid here",
                                "_senderusername" : "sender username here",
                                "timestamp" : "time comment was sent here"
                            }
                     }
              }
        },
    "posts" : {
           "serverId" : {
                  "postId" : {
                        "_imageLink" : "image Url here",
                        "_message" : "post description here",
                        "_sender" : "sender username here",
                        "_senderUID" : "sender uid here",
                        "_title" : "title of post here",
                        "timestamp" : "time messaged was sent here"
                     }
              }
        },
    "titles" : {
          "serverId" : {
                    "0" : "title Here",
                    "1" : "title Here for level 1"
                }
        },
}
```

**Database Rules:**
On top of the database, we also added sufficient security rules to ensure that server data should only be accessed by their members.
Server metadata such as server names and descriptions can also only be edited by the server owner.

### other libraries.

#### picasso

Picasso is a powerful image downloading and caching library for Android. For this application, we used it to load images into in-adapter ImageViews.
Using this library allowed us to worry less about the efficiency of loading all images into the adapters at once and the memory footprint of the app.

#### hdodenhof's circle image view

From https://github.com/hdodenhof/CircleImageView. This library allowed us to present the circular images shown throughout the app.
Combined with Picasso, it allowed us to present images like profile images in a clean manner.

#### material design

Originally, we thought that API 19 would have issues with implementing something introduced in API 21, but it turns out that some portions were backwards compatible.
Hence, we decided to import the library and use material design buttons for the app. However, as most parts of the app were already done, we decided not to upgrade the text fields. We also didn't use the navigation bar and floating action buttons due to some glitches on API 29 which were unable to be fixed.

#### arthurhub's android image cropper

From https://github.com/ArthurHub/Android-Image-Cropper. This library allows us to implement the image cropping capabilities when uploading pictures while posting or changing profile pictures.

#### gson + retrofit

From https://github.com/google/gson and https://github.com/square/retrofit. These were used in the notification portion of the application and helped us to send and recieve notifications through Firebase's Cloud Messaging service.

## how to use.
1) All users must register with a username, email address and password. (We have currently limited the total number of sign ups to 500 due to lack of funding for the database)
2) Next, log in with the registered details.
3) View Profile, View Server, Notification setting pages can be navigated through the bottom navigation bar
4) View Server Page
    - Tap on the floating action button to Join / Create a server!
    - Tap on the server card when you want to start your conversation!
    - Inside server you can Create Post, Chat and do you server settings
    - Tap on "Share" to generate a code and give it to people when they want to join the server!
5) View Profile Page
    - Tap on the floating action button to edit your profile!
    - Click Confirm to return to your profile and enjoy your new customized profile

## credits.

Mohammad Thabith (S10196396B):
- [Stage 1] Authentication (Login/Register/Reset password)
- [Stage 2] New Content Notifications (On new Post/Chat Message)
- [Stage 2] Server Notifications (On member join/leave/level up)
- [Stage 2] Notification Intents (Navigate straight to relevant activity)
- [Stage 2] Smart Notifications (Only notifies user based on current activity and server)

Danny Chan (S10196363F):
- [Stage 1] View/Edit Personal Profile
- [Stage 1] Upload/Edit Profile Pictures
- [Stage 2] Server View All Posts
- [Stage 2] Server Add New Post
- [Stage 2] Server Post Detail View
- [Stage 2] Server Post Comments

Eugene Long (S10193060J):
- [Stage 1] View/Join/Create Server
- [Stage 1] Server Chat
- [Stage 1] Server Share
- [Stage 1] Leave/Delete Servers
- [Stage 1/2] Custom Navigation Bar (Activity-based, not Fragment-based)
- [Stage 2] Server Member View (+ Detailed Member Profile View)
- [Stage 2] Server Settings Page
- [Stage 2] Server Progression (Exp/Leveling/Titles)
- [Stage 2] Code Optimisation/Refactoring/Organisation
