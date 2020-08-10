# Mogi Video Analytics Integration on Android, ExoPlayer

## Step 1.

Add 

```
https://github.com/rahullahoria/mogi-video-analytics-exoplayer/blob/master/ExoPlayer2Example/app/src/main/java/com/mogi/exoplayer2example/VideoAnalytics.java 
```

file to your project

## Step 2.

Create instance of VideoAnalytics file, where you are using ExoPlayer

```
VideoAnalytics va = new VideoAnalytics("Your APP ID","Current User Id");
```

Your APP ID : is already provide to you

Current User Id: User Id of the user, who is using the app. This id can be used to track back the use for marketing activities
 
## Step 3. 

```
player.addListener(va.getListener(player, "URL of the Video, M3U8 playlist file", "TITLE", "Description of Video", "TAGS"));
```

player : is instance of exoplayer.

TAGS: You can pass multiple tags, which can be seprated with comma ",". Eg. "hindi,emotional video"

## wherever, you have exoplayer define. You can use step 2 and 3, to start tracking the data over the video


# How to run the code ?

## Step 1.

Clone the current project. This will be our main project.
```
git clone https://github.com/rahullahoria/mogi-video-analytics-exoplayer.git
```

## Step 2.

This is Exoplayer fork, which have integration of Mogi Streaming Engine

```
git clone https://github.com/rahullahoria/mogi-exoplayer.git
```

## Step 3.


Next, add the following to your project's by Step 1 `settings.gradle` file, replacing
`path/to/exoplayer` with the path to your local copy of step 2:

```gradle
gradle.ext.exoplayerRoot = 'path/to/exoplayer-with-mogi-streaming-engine by step 2'
```



@Contact
Rajnish
8901414422


Good luck.
