# MAD-2020-threadapp
a social app made for an assignment.
codeveloped by Eugene L., Danny C. and Mohammad T.

Welcome to our Application [THREAD]

THREAD is a social application made to connect you to the world!
With server functions you will be able to speak to not just one but many! All At Once.
Customize your avatar and earn your level as you chat (to be implemented)

-----------------------------------------------------------------------------------------------------
Through this application we have used Firebase RealTime Database

RealTime Database Data Tree

```json
"thread" : {
    "users" : {
          "userId" : {
              "statusMessage" : "status message here",
              "profileImageURL" : "profile image URL here",
              "aboutMeMessage" : "description here",
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
                 "memberId" : "user id here"
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
        }
        
}
```


              
              
How to Use
1) Sign In with your Email address and password
2) You are now free to explore the application
3) View Profile & View Server pages can be navigated through the bottom navigation bar

4) View Server Page
  - Tap on the floating action button to Join / Create a server!
  - Tap on the server card when you want to start your conversation!
  - Tap on "Share" to generate a code and give it to people when they want to join the server!

5) View Profile Page
  - Tap on the floating action button to edit your profile!
  - Click Confirm to return to your profile and enjoy your new customized profile

