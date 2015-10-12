
package com.github.bstopp.kcjug.sling.twitter;

public class Account {

    public static final String USER_ID_PROP       = "user.id";
    public static final String SCREEN_NAME_PROP   = "screen.name";
    public static final String PROFILE_URL_PROP   = "profile.url";
    public static final String PROFILE_IMAGE_PROP = "profile.image";
    public static final String DESCRIPTION_PROP   = "description";
    public static final String FOLLOWERS_PROP     = "followers";
    public static final String FRIENDS_PROP       = "friends";

    private Long   userId;
    private String screenName;
    private String profileUrl;
    private String profileImage;
    private String description;
    private int    followers;
    private int    friends;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFriends() {
        return friends;
    }

    public void setFriends(int friends) {
        this.friends = friends;
    }

}
