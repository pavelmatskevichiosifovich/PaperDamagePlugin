package paper.damage.plugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class PaperDamagePlugin extends JavaPlugin implements Listener {

    private BukkitTask damageTask;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        damageTask = getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                double y = player.getLocation().getY();
                World world = player.getWorld();
                double heightThreshold = getHeightThreshold(world);

                if (y <= heightThreshold) {
                    // Наносим 2 сердца урона (4 единицы здоровья) каждые 0.5 секунд
                    if (player.getHealth() > 0) {
                        player.damage(4.0);
                    }
                }
            }
        }, 0L, 10L); // Выполнять задачу каждые 10 тиков (0.5 секунд)
    }

    @Override
    public void onDisable() {
        if (damageTask != null && !damageTask.isCancelled()) {
            damageTask.cancel();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        double y = player.getLocation().getY();
        double heightThreshold = getHeightThreshold(world);

        // Устанавливаем сообщение о смерти только если игрок погиб из-за высоты
        if (y <= heightThreshold) {
            String deathMessage = player.getName() + " выпал из мира";
            event.setDeathMessage(deathMessage);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                // Отменяем стандартное поведение для падения в пустоту
                event.setCancelled(true);
                if (player.getHealth() <= 0) {
                    // Проверяем, если игрок уже мертв, чтобы избежать повторных сообщений
                    player.setHealth(0); // Устанавливаем здоровье в 0 для вызова смерти
                }
            }
        }
    }

    private double getHeightThreshold(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            return -65; // Обычный мир
        } else if (world.getEnvironment() == World.Environment.NETHER) {
            return -1; // Ад
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            return -1; // Эндер мир
        } else {
            return 0; // По умолчанию
        }
    }
}
