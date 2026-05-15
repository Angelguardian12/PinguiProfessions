package bely.pinguiprofessions;

import bely.pinguiprofessions.database.DatabaseManager;
import bely.pinguiprofessions.gui.AdminGUI;
import bely.pinguiprofessions.gui.InvestigatorGUI;
import bely.pinguiprofessions.gui.PlayerGUI;
import bely.pinguiprofessions.listeners.CombatListener;
import bely.pinguiprofessions.listeners.MissionListener;
import bely.pinguiprofessions.listeners.PlayerListener;
import bely.pinguiprofessions.listeners.PotionListener;
import bely.pinguiprofessions.listeners.ProfessionListener;
import bely.pinguiprofessions.listeners.ShopListener;
import bely.pinguiprofessions.listeners.ExpListener;
import bely.pinguiprofessions.listeners.ClanListener;
import bely.pinguiprofessions.commands.ExpCommand;
import bely.pinguiprofessions.managers.BossBarManager;
import bely.pinguiprofessions.managers.CourseManager;
import bely.pinguiprofessions.managers.ClanIntegration;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.MissionsManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.managers.ReportManager;
import bely.pinguiprofessions.tasks.HologramTask;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class PinguiProfessions extends JavaPlugin {

    private static PinguiProfessions instance;
    private DatabaseManager databaseManager;
    private ProfessionManager professionManager;
    private LanguageManager languageManager;
    private CourseManager courseManager;
    private BossBarManager bossBarManager;
    private MissionsManager missionsManager;
    private ReportManager reportManager;
    private ClanIntegration clanIntegration;
    private AdminGUI adminGUI;
    private PlayerGUI playerGUI;
    private InvestigatorGUI investigatorGUI;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        getLogger().info("PinguiProfessions ha sido activado (v" + getDescription().getVersion() + ")");
        getLogger().info("Integrando con dependencias y cargando módulos...");
        
        languageManager = new LanguageManager(this);
        databaseManager = new DatabaseManager(this);
        professionManager = new ProfessionManager(this, databaseManager);
        courseManager = new CourseManager(this);
        bossBarManager = new BossBarManager(this);
        missionsManager = new MissionsManager(this);
        reportManager = new ReportManager(this);
        clanIntegration = new ClanIntegration(this, professionManager);
        adminGUI = new AdminGUI(this, courseManager);
        playerGUI = new PlayerGUI(this, professionManager);
        investigatorGUI = new InvestigatorGUI(this);
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this, professionManager, courseManager, clanIntegration), this);
        getServer().getPluginManager().registerEvents(new ProfessionListener(this, professionManager), this);
        getServer().getPluginManager().registerEvents(new PotionListener(this, professionManager), this);
        getServer().getPluginManager().registerEvents(new MissionListener(this, professionManager, missionsManager), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this, professionManager), this);
        getServer().getPluginManager().registerEvents(new bely.pinguiprofessions.listeners.ThiefListener(this, professionManager), this);
        getServer().getPluginManager().registerEvents(new bely.pinguiprofessions.listeners.InvestigatorListener(this, professionManager), this);
        getServer().getPluginManager().registerEvents(new ExpListener(this), this);
        getServer().getPluginManager().registerEvents(new ClanListener(this, clanIntegration, professionManager), this);
        
        if (getServer().getPluginManager().getPlugin("QuickShop") != null) {
            getServer().getPluginManager().registerEvents(new ShopListener(this, professionManager), this);
            getLogger().info("QuickShop detectado. Comerciantes activados.");
        }
        
        getCommand("profesiones").setExecutor(new bely.pinguiprofessions.commands.ProfesionesCommand(this, languageManager));
        getCommand("reportar").setExecutor(new bely.pinguiprofessions.commands.ReportCommand(this));
        getCommand("extraerxp").setExecutor(new ExpCommand(this));
        
        // Integración inicial con LuckPerms
        if (!getConfig().getBoolean("luckperms_groups_created", false)) {
            getLogger().info("Ejecutando creación inicial de grupos en LuckPerms...");
            String[] groups = {"herrero", "doctor", "alquimista", "tabernero", "caballero", "comerciante", "ladron", "comisario"};
            for (String group : groups) {
                getServer().dispatchCommand(getServer().getConsoleSender(), "lp creategroup " + group);
            }
            getConfig().set("luckperms_groups_created", true);
            saveConfig();
        }
        
        // Ejecutar tarea de holograma cada 10 ticks (medio segundo)
        new HologramTask(this, courseManager, professionManager).runTaskTimer(this, 20L, 10L);
    }

    @Override
    public void onDisable() {
        getLogger().info("PinguiProfessions ha sido desactivado.");
        if (bossBarManager != null) {
            bossBarManager.removeAll();
        }
        if (professionManager != null) {
            professionManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public AdminGUI getAdminGUI() {
        return adminGUI;
    }

    public PlayerGUI getPlayerGUI() {
        return playerGUI;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public InvestigatorGUI getInvestigatorGUI() {
        return investigatorGUI;
    }

    public static PinguiProfessions getInstance() {
        return instance;
    }
}
