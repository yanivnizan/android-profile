android-profile
===============

*This project is a part of [The SOOMLA Project](http://project.soom.la) which is a series of open source initiatives with a joint goal to help mobile game developers build engaging and monetizing games more easily.*

The gist:

```Java
    // decide on rewards (here 1 "sword" virtual item) for social action
    Reward reward = new VirtualItemReward([id], "Update Status for item", 1, "sword");

    // optional reward on each action, select from available social providers
    SoomlaProfile.getInstance().login([activity], IProvider.Provider.FACEBOOK, [reward]);

    // after login, the user profile on the selected providers is available locally
    UserProfile userProfile = SoomlaProfile.getInstance().getStoredUserProfile();

    // on successful login event, you can perform actions on the provider
    // this will post to Facebook, upon completion, the user will get 1 sword!
    SoomlaProfile.getInstance().updateStatus(IProvider.Provider.FACEBOOK, "cool game!", reward);
```

## android-profile

**android-profile** is an open code initiative as part of The SOOMLA Project. It is a Java API that unifies interaction with social and identity provider APIs, and optionally ties it together with the game's virtual economy.
This enables to easily reward players with social actions they perform in-game, and to leverage user profiles.

![SOOMLA's Profile Module](http://know.soom.la/img/tutorial_img/soomla_diagrams/Profile.png)

## Getting Started (With sources)

1. Add the jars from the [build](https://github.com/soomla/android-profile/tree/master/build) folder to your project.

1. Make the following changes to your AndroidManifest.xml:

  Set `SoomlaApp` as the main Application by placing it in the `application` tag:

    ```xml
    <application ...
                 android:name="com.soomla.SoomlaApp">
    ```

1. Initialize `Soomla` with a secret that you chose to encrypt the user data. (For those who came from older versions, this should be the same as the old "custom secret"):

    ```Java
     Soomla.initialize("[YOUR CUSTOM GAME SECRET HERE]");
    ```
    > The secret is your encryption secret for data saved in the DB.

1. Initialize `SoomlaProfile`

  ```Java
    SoomlaProfile.getInstance().initialize();
  ```

1. If integrating a virtual economy with the store module, please see [android-store](https://github.com/soomla/android-store) for store setup.

1. Refer to the [next section](https://github.com/soomla/android-profile#whats-next-selecting-social-providers) for information of selecting social providers and setting them up.

Cool, almost there, on to provider selection!


## What's next? Selecting Social Providers

**android-profile** is structured to support multiple social networks (Facebook, Twitter, etc.), at the time of writing this the framework only supports Facebook integration.
We use the [Simple Facebook project](https://github.com/sromku/android-simple-facebook) to support this integration.

### Facebook

Facebook is supported out-of-the-box, you just have to follow the next steps to make it work:

1. Import the Facebook SDK for Android into your project and setup all the relevant information (Application ID, etc).

    1. For more information regarding this refer to [Facebook SDK for Android](https://developers.facebook.com/docs/android)

    1. SOOMLA uses [Android Studio](https://developer.android.com/sdk/installing/studio.html), in this case you can extract the Facebook SDK into your project folder and then it's simply a case of importing the `iml` file in the Facebook SDK folder into your project
1. Make the following changes in `AndroidManifest.xml`:

      ```xml
      ...

      <application ...
          <activity android:name="com.soomla.profile.social.facebook.SoomlaFacebook$SoomlaFBActivity" android:theme="@android:style/Theme.Translucent.NoTitleBar.Fullscreen">
        </activity>
      </application>
      ```

## UserProfile

As part of a login call to a provider, Soomla will internally try to also fetch the online user profile details via
`UserProfile` and store them in the secure [Soomla Storage](https://github.com/soomla/android-store#storage--meta-data)
Later, this can be retrieved locally (in offline mode) via:

```java
UserProfile userProfile = SoomlaProfile.getInstance().getStoredUserProfile(IProvider.Provider.FACEBOOK)
```

This can throw a `UserProfileNotFoundException` if something strange happens to the local storage, in that case, you need to require a new login to get the `UserProfile` again.

## Rewards feature

One of the big benefits of using Soomla's profile module for social networks interactions is that you can easily tie it in with the game's virtual economy.
This is done by the ability to specify a `Reward` (perhaps more specifically, a `VirtualItemReward`) to most social actions defined in `SoomlaProfile`.

For example, to reward a user with a "sword" virtual item upon login to Facebook:
```Java
Reward reward = new VirtualItemReward([id], "Update Status for sword", 1, "sword");
SoomlaProfile.getInstance().login([activity], IProvider.Provider.FACEBOOK, reward);
```

Once login completes sucessfully (wait for `LoginFinishedEvent`, see below on events), the reward will be automatically given, and synchronized with Soomla's storage.

The reward ID is something you manage and should be unique, much like virtual items.


## Debugging

In order to debug android-profile, set `SoomlaConfig.logDebug` to `true`. This will print all of _android-profile's_ debugging messages to logcat.

## Storage

The on-device storage is encrypted and kept in a SQLite database. SOOMLA is preparing a cloud-based storage service that will allow this SQLite to be synced to a cloud-based repository that you'll define.

## Security


If you want to protect your game from 'bad people' (and who doesn't?!), you might want to follow some guidelines:

+ SOOMLA keeps the game's data in an encrypted database. In order to encrypt your data, SOOMLA generates a private key out of several parts of information. The Custom Secret is one of them. SOOMLA recommends that you provide this value when initializing `Soomla` and before you release your game. BE CAREFUL: You can change this value once! If you try to change it again, old data from the database will become unavailable.
+ Following Google's recommendation, SOOMLA also recommends that you split your public key and construct it on runtime or even use bit manipulation on it in order to hide it. The key itself is not secret information but if someone replaces it, your application might get fake messages that might harm it.

## Event Handling


For event handling, we use Square's great open-source project [otto](http://square.github.com/otto/). In ordered to be notified of profile related events, you can register for specific events and create your game-specific behavior to handle them.

> Your behavior is an addition to the default behavior implemented by SOOMLA. You don't replace SOOMLA's behavior.

In order to register for events:

1. In the class that should receive the event create a function with the annotation '@Subscribe'. Example:

    ```Java
    @Subscribe
    public void onLoginFinishedEvent(LoginFinishedEvent loginFinishedEvent) {
        ...
    }
    ```

2. You'll also have to register your class in the event bus (and unregister when needed):

   ```Java
   BusProvider.getInstance().register(this);
   ```

   ```Java
   BusProvider.getInstance().unregister(this);
   ```

> If your class is an Activity, register in 'onResume' and unregister in 'onPause'

You can find a full event handler example [here](https://github.com/soomla/android-profile/blob/master/SoomlaAndroidExample/src/com/soomla/example/ExampleEventHandler.java).

[List of events](https://github.com/soomla/android-profile/tree/master/SoomlaAndroidProfile/src/com/soomla/profile/events)

[Full documentation and explanation of otto](http://square.github.com/otto/)

## Example Project

The **android-profile** project contains an [example project](https://github.com/soomla/android-profile/tree/master/SoomlaAndroidExample) which shows most of the functionality Profile provides, and the correct setup.
In order to run the project follow this steps:

1. Open the `SoomlaAndroidExample` folder in Android Studio (IntelliJ), it contains an IntelliJ project
1. Setup SDK and out folder, if necessary
1. Run the project

## Contribution


We want you!

Fork -> Clone -> Implement -> Insert Comments -> Test -> Pull-Request.

We have great RESPECT for contributors.

## Code Documentation


android-profile follows strict code documentation conventions. If you would like to contribute please read our [Documentation Guidelines](https://github.com/soomla/android-store/blob/master/documentation.md) and follow them. Clear, consistent  comments will make our code easy to understand.

## SOOMLA, Elsewhere ...

+ [Framework Website](http://www.soom.la/)
+ [On Facebook](https://www.facebook.com/pages/The-SOOMLA-Project/389643294427376).
+ [On AngelList](https://angel.co/the-soomla-project)


## License

Apache License. Copyright (c) 2012-2014 SOOMLA. http://www.soom.la
+ http://opensource.org/licenses/Apache-2.0
