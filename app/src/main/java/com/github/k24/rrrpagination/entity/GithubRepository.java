package com.github.k24.rrrpagination.entity;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by k24 on 2016/12/06.
 */
@RealmClass
public class GithubRepository implements RealmModel {
    @PrimaryKey
    public long id;
    public String full_name;
    public String html_url;
    public String description;
}
