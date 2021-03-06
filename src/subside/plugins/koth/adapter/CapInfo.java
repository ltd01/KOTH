package subside.plugins.koth.adapter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import subside.plugins.koth.Lang;
import subside.plugins.koth.adapter.captypes.Capper;
import subside.plugins.koth.events.KothCapEvent;
import subside.plugins.koth.events.KothLeftEvent;
import subside.plugins.koth.hooks.HookManager;
import subside.plugins.koth.utils.MessageBuilder;

public class CapInfo {
    private @Getter @Setter int timeCapped;
    
    private @Getter @Setter Capper capper;
    private @Getter RunningKoth runningKoth;
    private @Getter Capable captureZone;
    private @Getter Class<? extends Capper> ofType;
    private boolean sendMessages;
    
    public CapInfo(RunningKoth runningKoth, Capable captureZone, Class<? extends Capper> ofType, boolean sendMessages){
        this.runningKoth = runningKoth;
        this.captureZone = captureZone;
        this.sendMessages = sendMessages;
        
        if(ofType != null){
            this.ofType = ofType;
        } else {
            this.ofType = KothHandler.getInstance().getCapEntityRegistry().getPreferedClass();
        }
    }
    
    public CapInfo(RunningKoth runningKoth, Capable captureZone, Class<? extends Capper> ofType){
    	this(runningKoth, captureZone, ofType, true);
    }
    
    public CapInfo(RunningKoth runningKoth, Capable captureZone){
    	this(runningKoth, captureZone, null);
    }

    
    /** Override this if you want to use a different type of capper
     * @param playerList a list of players to choose from
     * @return the correct capper type
     */
    public Capper getRandomCapper(List<Player> playerList){
        return KothHandler.getInstance().getCapEntityRegistry().getCapper(ofType, playerList);
    }
    
    /** Gets updated every single tick
     * 
     */
    public void update(){
        if(capper != null && capper.getObject() != null){
            if(capper.areaCheck(captureZone)){
                timeCapped++;
                return;
            }
            KothLeftEvent event = new KothLeftEvent(runningKoth, captureZone, capper, timeCapped);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                timeCapped++;
                return;
            }
            
        
        

            if (event.getNextCapper() == null) {
//                new MessageBuilder(Lang.KOTH_PLAYING_LEFT).maxTime(maxRunTime).time(getTimeObject()).player(pCapper.getName()).koth(koth).shouldExcludePlayer().buildAndBroadcast();
//                if (pCapper.isOnline()) {
//                    new MessageBuilder(Lang.KOTH_PLAYING_LEFT_CAPPER).maxTime(maxRunTime).time(getTimeObject()).player(pCapper.getName()).koth(koth).buildAndSend(pCapper.getPlayer());
//                }
            	if(sendMessages)
            		runningKoth.fillMessageBuilder(new MessageBuilder(Lang.KOTH_PLAYING_LEFT)).capper(getName()).shouldExcludePlayer().buildAndBroadcast();
                capper = null;
                timeCapped = 0;
            } else {
                timeCapped++;
                capper = event.getNextCapper();
            }   
        } else {
            List<Player> insideArea = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (captureZone.isInArea(player)) {
                    if(HookManager.getHookManager().canCap(player)) {
                        insideArea.add(player);
                    }
                }
            }
            if (insideArea.size() < 1) {
                return;
            }
            
            
            Capper capper = getRandomCapper(insideArea);
            if(capper == null)
            	return;
            KothCapEvent event = new KothCapEvent(runningKoth, captureZone, insideArea, capper);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            this.capper = event.getNextCapper();
            
        	if(sendMessages)
        		runningKoth.fillMessageBuilder(new MessageBuilder(Lang.KOTH_PLAYING_CAP_START)).capper(getName())/*.shouldExcludePlayer()*/.buildAndBroadcast();
//            if (Bukkit.getPlayer(cappingPlayer) != null) {
//                new MessageBuilder(Lang.KOTH_PLAYING_PLAYERCAP_CAPPER).maxTime(maxRunTime).capper(cappingPlayer).koth(koth).time(getTimeObject()).buildAndSend(Bukkit.getPlayer(cappingPlayer));
//            }
            // TO-DO
        }
    }
    
    /**
     * @return the name of the object (Playername for players, Factionname for factions)
     */
    public String getName(){
    	if(capper != null && capper.getObject() != null){
    		return capper.getName();
    	} else {
    		return "None";
    	}
    }
}