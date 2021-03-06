package com.minehut.warzone.module.modules.visibility;

import com.google.common.base.Optional;
import com.minehut.warzone.event.MatchEndEvent;
import com.minehut.warzone.event.MatchStartEvent;
import com.minehut.warzone.event.PlayerChangeTeamEvent;
import com.minehut.warzone.event.PlayerVisibilityChangeEvent;
import com.minehut.warzone.match.Match;
import com.minehut.warzone.match.MatchState;
import com.minehut.warzone.module.Module;
import com.minehut.warzone.module.modules.team.TeamModule;
import com.minehut.warzone.util.Teams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;

public class Visibility implements Module {

    private final Match match;

    protected Visibility(Match match) {
        this.match = match;
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                player.showPlayer(otherPlayer);
            }
        }
    }

    private void resetVisibility(Player viewer, Player toSee, Optional<TeamModule> newTeam) {
        try {
            if (match.getState().equals(MatchState.PLAYING)) {
                Optional<TeamModule> viewerTeam = Teams.getTeamByPlayer(viewer);
                Optional<TeamModule> seeTeam = Teams.getTeamByPlayer(toSee);
                if (viewerTeam.isPresent() && viewerTeam.get().isObserver()) {
//                    if (seeTeam.isPresent() && seeTeam.get().isObserver() && Settings.getSettingByName("Observers") != null && Settings.getSettingByName("Observers").getValueByPlayer(viewer).getValue().equalsIgnoreCase("none")) {
//                        viewer.hidePlayer(toSee);
//                    } else {
                        viewer.showPlayer(toSee);
//                    }
                } else if (newTeam.isPresent() && newTeam.get().isObserver()) {
                    viewer.hidePlayer(toSee);
                } else {
                    viewer.showPlayer(toSee);
                }

            } else {
//                if (Settings.getSettingByName("Observers") != null && Settings.getSettingByName("Observers").getValueByPlayer(viewer).getValue().equalsIgnoreCase("none")) {
//                    viewer.hidePlayer(toSee);
//                } else {
                    viewer.showPlayer(toSee);
//                }
            }
        } catch (NullPointerException e) {
            viewer.showPlayer(toSee);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            this.resetVisibility(viewer, event.getPlayer(), Teams.getTeamByPlayer(event.getPlayer()));
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            this.resetVisibility(event.getPlayer(), online, Teams.getTeamByPlayer(online));
        }
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player toSee : Bukkit.getOnlinePlayers()) {
                this.resetVisibility(viewer, toSee, Teams.getTeamByPlayer(toSee));
            }
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player toSee : Bukkit.getOnlinePlayers()) {
                this.resetVisibility(viewer, toSee, Teams.getTeamByPlayer(toSee));
            }
        }
    }

    @EventHandler
    public void onPlayerChangeTeam(PlayerChangeTeamEvent event) {
        Player switched = event.getPlayer();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            this.resetVisibility(viewer, switched, event.getNewTeam());
            this.resetVisibility(switched, viewer, Teams.getTeamByPlayer(viewer));
        }
    }

    @EventHandler
    public void onPlayerVisibilityChange(PlayerVisibilityChangeEvent event) {
        for (Player toSee : Bukkit.getOnlinePlayers()) {
            this.resetVisibility(event.getPlayer(), toSee, Teams.getTeamByPlayer(toSee));
        }
    }
}
