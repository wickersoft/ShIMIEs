/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.shimie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import zedly.shimie.HTTP.HTTPResponse;

/**
 *
 * @author Dennis
 */
public class Watcher implements Listener {

    private static final Watcher instance = new Watcher();
    private static int remainingSamples = 0;
    private static List<ImieDetails> recordedImies = new ArrayList<>();
    private static CommandSender reportTo;

    public static Watcher instance() {
        return instance;
    }

    public boolean startRecording(CommandSender reportTo, int samples) {
        if (remainingSamples > 0) {
            return false;
        }

        Watcher.reportTo = reportTo;
        recordedImies.clear();
        remainingSamples = samples;
        return true;
    }

    @EventHandler
    public void onImie(InventoryMoveItemEvent imie) {
        if (remainingSamples <= 0) {
            return;
        }

        recordedImies.add(new ImieDetails(imie));

        if (--remainingSamples == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("data={\"imies\":[\n\n");

            for (int i = 0; i < recordedImies.size(); i++) {
                if (i > 0) {
                    sb.append(",\n\n");
                }
                sb.append(recordedImies.get(i).toString());
            }
            sb.append("]}");

            try {
                HTTPResponse response = HTTP.http("http://wickersoft.com/imies.php?type=imies", sb.toString(), "application/x-www-form-urlencoded", 3000);
                String url = new String(response.content);
                reportTo.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + "Done! Data available at " + url);
            } catch (IOException ex) {
                reportTo.sendMessage(ChatColor.GOLD + "ShIMIE: " + ChatColor.GRAY + "Unable to post timings to wickersoft.com!");
            }
        }
    }

    private class ImieDetails {

        private final Location initLoc, srcLoc, destLoc;
        private final InventoryType initType, srcType, destType;
        private final ItemStack is;
        private final long captureNanos;

        public ImieDetails(InventoryMoveItemEvent evt) {
            initLoc = evt.getInitiator().getLocation();
            initType = evt.getInitiator().getType();

            srcLoc = evt.getSource().getLocation();
            srcType = evt.getSource().getType();

            destLoc = evt.getDestination().getLocation();
            destType = evt.getDestination().getType();

            is = evt.getItem();
            captureNanos = System.nanoTime();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("{\n    \"initType\":\"");
            sb.append(initType);
            sb.append("\",\n    \"initLoc\":");
            sb.append(ShIMIE.locString(initLoc));
            sb.append(",\n    \"srcType\":\"");
            sb.append(srcType);
            sb.append("\",\n    \"srcLoc\":");
            sb.append(ShIMIE.locString(srcLoc));
            sb.append(",\n    \"destType\":\"");
            sb.append(destType);
            sb.append("\",\n    \"destLoc\":");
            sb.append(ShIMIE.locString(destLoc));
            sb.append(",\n    \"stack\":\"");
            sb.append(is);
            sb.append("\",\n    \"nanos\":\"");
            sb.append(captureNanos);
            sb.append("\"\n}");

            return sb.toString();
        }
    }

}
