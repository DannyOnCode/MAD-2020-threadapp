# MAD-2020-threadapp
a social app made for an assignment.
codeveloped by Eugene Long (S10193060J), Danny Chan (S10196363F) and Mohammad Thabith (S10196396).

Welcome to our Application [THREAD]

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

-----------------------------------------------------------------------------------------------------
Through this application we have used Firebase Realtime Database

## Realtime Database Data Tree

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


              
              
## How to Use
1) Sign In with your Email address and password
2) You are now free to explore the application
3) View Profile, View Server, Notification setting pages can be navigated through the bottom navigation bar

4) View Server Page
  - Tap on the floating action button to Join / Create a server!
  - Tap on the server card when you want to start your conversation!
  - Inside server you can Create Post, Chat and do you server settings
  - Tap on "Share" to generate a code and give it to people when they want to join the server!

5) View Profile Page
  - Tap on the floating action button to edit your profile!
  - Click Confirm to return to your profile and enjoy your new customized profile




## Credits

Thabith:
- Login/Register Activities
- Reset Password

Danny:
- View/Edit Profile Activities
- Profile pics
- View Post Details
- Create Post

Eugene:
- View/Add (Join/Create) Servers, Chat Activities
- Share Server Code
- Leave/Delete Servers
