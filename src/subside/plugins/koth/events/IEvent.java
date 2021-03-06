package subside.plugins.koth.events;

import subside.plugins.koth.adapter.Koth;

/**
 * @author Thomas "SubSide" van den Bulk
 *
 */
public interface IEvent {
    

    /** Get the KoTH object
     * 
     * @return          The KoTH object
     */
    public Koth getKoth();
}
