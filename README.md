# MAD-2020-threadapp
a social app made for an assignment.
codeveloped by Eugene Long (S10193060J), Danny Chan (S10196363F) and Mohammad Thabith (S10196396).

Welcome to our Application [THREAD]

*An assignment for partial fulfillment of the coursework of Mobile Applications Development of AY2019/20*

Feeling sociable? Want to tell the world how you feel? Connect yourself with the rest of the world?
Thread. is most likely what you are searching for! Our goal is to make you feel connected to the world and
also to make you. Feel proud of being in a community you are in! While doing all those, rank yourself up as you chat
with our built-in leveling system and show off to all your friends just how awesome you are!

Why are you still reading this? Jump right in to experience it for yourself. After all, this is a social app
for you to talk to us!

What can you look forward to?!
Registering, Loggin, Profile, Profile Editing, Chat function,
Post Function, Server Settings, Server Creation, Notification, Server Titles, Server Levels!

With all that, I hope you will enjoy our application and help you bring the people you wish to know ever so closer to you!


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
