package model;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole extends Model implements Role
{
  /**
   * 
   */
  private static final long serialVersionUID = -206551239908446959L;

  @Id
  public Long id;

  public String name;

  public static final Finder<Long, SecurityRole> find = new Finder<Long, SecurityRole>(
    Long.class,
    SecurityRole.class);

  public String getName()
  {
    return name;
  }

  public static SecurityRole findByName(String name)
  {
    return find.where().eq("name", name).findUnique();
  }
}
