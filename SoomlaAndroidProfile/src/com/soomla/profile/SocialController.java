/*
 * Copyright (C) 2012-2014 Soomla Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.soomla.profile;

import android.app.Activity;
import android.graphics.Bitmap;

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.events.social.GetContactsFailedEvent;
import com.soomla.profile.events.social.GetContactsFinishedEvent;
import com.soomla.profile.events.social.GetContactsStartedEvent;
import com.soomla.profile.events.social.GetFeedFailedEvent;
import com.soomla.profile.events.social.GetFeedFinishedEvent;
import com.soomla.profile.events.social.GetFeedStartedEvent;
import com.soomla.profile.events.social.SocialActionFailedEvent;
import com.soomla.profile.events.social.SocialActionFinishedEvent;
import com.soomla.profile.events.social.SocialActionStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.rewards.Reward;

import java.util.List;

/**
 * A class that loads all social providers and performs social
 * actions on with them.  This class wraps the provider's social
 * actions in order to connect them to user profile data and rewards.
 *
 * Inheritance: {@link com.soomla.profile.SocialController} >
 * {@link com.soomla.profile.AuthController} >
 * {@link com.soomla.profile.ProviderLoader}
 */
public class SocialController extends AuthController<ISocialProvider> {

    /**
     * Constructor
     *
     * Loads all social providers
     ** @param usingExternalProvider {@link SoomlaProfile#initialize}
     *
     */
    public SocialController(boolean usingExternalProvider) {
        super(usingExternalProvider);
        if (!usingExternalProvider && !loadProviders("com.soomla.profile.social.facebook.SoomlaFacebook")) {
            String msg = "You don't have a ISocialProvider service attached. " +
                    "Decide which ISocialProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            SoomlaUtils.LogDebug(TAG, msg);
        }
    }

    /**
     * Shares the given status to the user's feed
     *
     * @param provider the provider to use
     * @param status the text to share
     * @param reward the reward to grant for sharing
     * @throws ProviderNotFoundException
     */
    public void updateStatus(final IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStatusType = ISocialProvider.SocialActionType.UPDATE_STATUS;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStatusType));
        socialProvider.updateStatus(status, new SocialCallbacks.SocialActionListener() {
            @Override
            public void success() {
                BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStatusType));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStatusType, message));
            }
        });
    }

    /**
     * Shares a story to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider The provider on which to update user's story
     * @param message The main text which will appear in the story
     * @param name The headline for the link which will be integrated in the
     *             story
     * @param caption The sub-headline for the link which will be
     *                integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link The link which will be integrated into the user's story
     * @param picture a Link to a picture which will be featured in the link
     * @param reward The reward which will be granted to the user upon a
     *               successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     * supported by the framework
     */
    public void updateStory(final IProvider.Provider provider, String message, String name, String caption, String description,
                            String link, String picture, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStoryType = ISocialProvider.SocialActionType.UPDATE_STORY;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStoryType));
        socialProvider.updateStory(message, name, caption, description, link, picture,
                new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStoryType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStoryType, message));
                    }
                }
        );
    }

    /**
     * Shares a photo to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider The provider to use
     * @param message A text that will accompany the image
     * @param fileName The desired image's file name
     * @param bitmap The image to share
     * @param jpegQuality The image's numeric quality
     * @param reward The reward to grant for sharing the photo
     * @throws ProviderNotFoundException if the supplied provider is not
     * supported by the framework
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UPLOAD_IMAGE;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, uploadImageType));
        socialProvider.uploadImage(message, fileName, bitmap, jpegQuality, new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, uploadImageType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, uploadImageType, message));
                    }
                }
        );
    }

    /**
     * Shares a photo to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider The provider to use
     * @param message A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param reward The reward to grant for sharing the photo
     * @throws ProviderNotFoundException if the supplied provider is not
     * supported by the framework
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, String filePath,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UPLOAD_IMAGE;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, uploadImageType));
        socialProvider.uploadImage(message, filePath, new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, uploadImageType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, uploadImageType, message));
                    }
                }
        );
    }

    /**
     * Fetches the user's contact list
     *
     * @param provider The provider to use
     * @param reward The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     * supported by the framework
     */
    public void getContacts(final IProvider.Provider provider,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getContactsType = ISocialProvider.SocialActionType.GET_CONTACTS;
        BusProvider.getInstance().post(new GetContactsStartedEvent(provider, getContactsType));
        socialProvider.getContacts(new SocialCallbacks.ContactsListener() {
               @Override
               public void success(List<UserProfile> contacts) {
                   BusProvider.getInstance().post(new GetContactsFinishedEvent(provider, getContactsType, contacts));

                   if (reward != null) {
                       reward.give();
                   }
               }

               @Override
               public void fail(String message) {
                   BusProvider.getInstance().post(new GetContactsFailedEvent(provider, getContactsType, message));
               }
            }
        );
    }

    /**
     * Fetches the user's feed.
     *
     * @param provider The provider to use
     * @param reward The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     * supported by the framework
     */
    public void getFeed(final IProvider.Provider provider,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getFeedType = ISocialProvider.SocialActionType.GET_FEED;
        BusProvider.getInstance().post(new GetFeedStartedEvent(provider, getFeedType));
        socialProvider.getFeed(new SocialCallbacks.FeedListener() {
               @Override
               public void success(List<String> feedPosts) {
                   BusProvider.getInstance().post(new GetFeedFinishedEvent(provider, getFeedType, feedPosts));

                   if (reward != null) {
                       reward.give();
                   }
               }

               @Override
               public void fail(String message) {
                   BusProvider.getInstance().post(new GetFeedFailedEvent(provider, getFeedType, message));
               }
           }
        );
    }

    /**
     * Opens up a provider page to "like" (external), and grants the user the supplied reward
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param pageName The page to open up
     * @param reward The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     * supported by the framework
     */
    public void like(final Activity activity, final IProvider.Provider provider,
                     String pageName,
                     final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);
        socialProvider.like(activity, pageName);

        if (reward != null) {
            reward.give();
        }
    }

    private static final String TAG = "SOOMLA SocialController";
}
