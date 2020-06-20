package me.renes.RandomPvPTeleport;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class RandomPvPTeleport extends JavaPlugin {

    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        loadConfig();
        getCommand("wild").setExecutor(this);
    }

    @Override
    public void onDisable() {


    }
    private HashMap<UUID, Long> onCooldown = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("wild")) return true;
        if (!(sender instanceof Player)) return true;
        if (!sender.hasPermission("wild.use")) return true;
        switch (args.length) {
            case 0:
                if(onCooldown.isEmpty() || !onCooldown.containsKey(((Player) sender).getUniqueId()) || (System.currentTimeMillis() - onCooldown.get(((Player) sender).getUniqueId())) > cooldown*1000) {
                    Random r = new Random();
                    Location toTP = ((Player) sender).getLocation();
                    double tpDistance = 0;
                    Material spawnBlock = null;
                    while(spawnBlock == null) {
                            long xPos = Math.round(r.nextDouble() * (maxLoc.getBlockX() - minLoc.getBlockX()) + minLoc.getBlockX());
                            long zPos = Math.round(r.nextDouble() * (maxLoc.getBlockZ() - minLoc.getBlockZ()) + minLoc.getBlockZ());
                            toTP = new Location(minLoc.getWorld(), xPos, 0, zPos);
                            if(toTP.distance(center)<minDist) {
                                continue;
                            }
                        int yLevel = 256;
                        toTP.setY(yLevel);
                        if(toTP.getBlock().getBiome().name().toLowerCase().contains("ocean")) {
                            continue;
                        }
                        while (!toTP.getBlock().getType().isSolid()) {
                            toTP.setY(toTP.getY() - 1);
                        }

                        spawnBlock = toTP.getBlock().getType();
                    }
                    toTP.add(0.5, 1, 0.5);

                    ((Player) sender).teleport(toTP);
                    sender.sendMessage(teleportmsg);
                    if(onCooldown.containsKey(((Player) sender).getUniqueId())) {
                        onCooldown.replace(((Player) sender).getUniqueId(), System.currentTimeMillis());
                    } else {
                        onCooldown.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
                    }
                } else {
                    sender.sendMessage(color("&cYou cannot teleport into the wild, you need to wait " + getWaitTime((cooldown*1000)-(System.currentTimeMillis() - onCooldown.get(((Player) sender).getUniqueId())))));
                }
                break;
            case 1:
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("wild.admin")) {
                        reloadConfig();
                        loadConfig();
                        sender.sendMessage(color("&3RNDPVPTP &7| &aReloaded Config"));
                    }
                } else if (args[0].equalsIgnoreCase("clear")) {
                    if (sender.hasPermission("wild.admin")) {
                        onCooldown.remove(((Player) sender).getUniqueId());
                        sender.sendMessage(color("&3RNDPVPTP &7| &aCleared you from list"));
                    }
                }
                break;
        }
        return true;
    }

    private String getWaitTime(long l) {
        long seconds = l/1000;
        long minutes = 0;
        if(seconds>60) {
            minutes = (long) Math.floor(seconds/60);
        }
        seconds -= minutes*60;
        if(minutes > 0) {
            return minutes + " m, " + seconds + " secs";
        }
        return seconds + " secs";
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private Location minLoc;
    private Location maxLoc;
    private int minDist;
    private Location center;
    private String teleportmsg;
    private int cooldown;
    private void loadConfig() {
        ConfigurationSection cs = getConfig().getConfigurationSection("teleportinfo");
        World w = getServer().getWorld(cs.getString("worldName"));
        int minX = cs.getInt("Xmin");
        int minZ = cs.getInt("Zmin");
        int maxX = cs.getInt("Xmax");
        int maxZ = cs.getInt("Zmax");
        minLoc = new Location(w,Math.min(minX,maxX), 0, Math.min(minZ,maxZ));
        maxLoc = new Location(w,Math.max(minX,maxX), 0, Math.max(minZ,maxZ));
        center = new Location(
                getServer().getWorld(cs.getString("worldName")),
                cs.getDouble("CenterX"), 0, cs.getDouble("CenterXZ"));
        minDist = cs.getInt("minDistTeleport");
        teleportmsg = color(cs.getString("message"));
        cooldown = cs.getInt("cooldown");
    }
}
