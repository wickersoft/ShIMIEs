package zedly.shimie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

public class ShIMIE extends JavaPlugin {

    private static ShIMIE instance;
    private static final Watcher watcher = Watcher.instance();

    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(watcher, this);
    }

    public boolean onCommand(CommandSender sender, Command command, String commandlabel, String[] args) {
        if (args.length == 2 && args[0].equals("imies") && args[1].matches("\\d+")) {
            int numImies = Integer.parseInt(args[1]);
            watcher.startRecording(sender, numImies);
            sender.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + " Now recording IMIEs");
        } else if (args.length == 2 && args[0].equals("tiles")) {
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + "World not found!");
                return true;
            }

            List<TileDetails> tileDetails = new ArrayList<>();
            Chunk[] chunks = world.getLoadedChunks();
            for (Chunk ch : chunks) {
                BlockState[] tiles = ch.getTileEntities();
                for (BlockState tile : tiles) {
                    tileDetails.add(new TileDetails(tile));
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("data={\"tiles\":[\n\n");

            for (int i = 0; i < tileDetails.size(); i++) {
                if (i > 0) {
                    sb.append(",\n\n");
                }
                sb.append(tileDetails.get(i).toString());
            }
            sb.append("]}");

            try {
                HTTP.HTTPResponse response = HTTP.http("http://wickersoft.com/imies.php?type=tiles", sb.toString(), "application/x-www-form-urlencoded", 3000);
                String url = new String(response.content);
                sender.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + "Done! Data available at " + url);
            } catch (IOException ex) {
                sender.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + "Unable to post tiles to wickersoft.com!");
            }
        } else {
            sender.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + "/sh (imies <number> / tiles)");
        }
        return true;
    }

    public static String locString(Location loc) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"world\":\"");
        sb.append(loc.getWorld().getName());
        sb.append("\",\"x\":");
        sb.append(loc.getX());
        sb.append(",\"y\":");
        sb.append(loc.getY());
        sb.append(",\"z\":");
        sb.append(loc.getZ());
        sb.append("}");
        return sb.toString();
    }

    private class TileDetails {

        private final String type;
        private final Location loc;

        public TileDetails(BlockState tile) {
            type = tile.getClass().getCanonicalName();
            loc = tile.getLocation();
        }

        public String toString() {
            return "{\n    \"type\":\"" + type + "\",\n    \"loc\":" + locString(loc) + "\n}";
        }
    }

}
