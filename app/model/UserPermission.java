package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Permission;

@Entity
public class UserPermission extends Model implements Permission
{
  /**
   * 
   */
  private static final long serialVersionUID = 3080837459582832218L;

  @Id
  public Long id;

  @Column(name = "permission_value")
  public String value;

  public static final Model.Finder<Long, UserPermission> find = new Model.Finder<Long, UserPermission>(
    Long.class,
    UserPermission.class);

  public String getValue()
  {
    return value;
  }

  public static UserPermission findByValue(String value)
  {
    return find.where().eq("value", value).findUnique();
  }
}