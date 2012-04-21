(ns horsetail.core
  (:require [cloft.cloft :as c])
  ;(:require [clojure.core.match :as m])
  (:require [swank.swank])
  (:require [clojure.string :as s])
  (comment (:import [org.bukkit.command CommandExecuter CommandSender Command]))
  (:import [org.bukkit Bukkit Material])
  (:import [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity Item LightningStrike LivingEntity
            MagmaCube Minecart Monster MushroomCow NPC Painting Pig PigZombie
            Player PoweredMinecart Projectile Sheep Silverfish Skeleton Slime
            SmallFireball Snowball Snowman Spider Squid StorageMinecart
            ThrownPotion TNTPrimed Vehicle Villager WaterMob Weather Wolf
            Zombie])
  (:import [org.bukkit.event.entity EntityDamageByEntityEvent
            EntityDamageEvent$DamageCause])
  (:import [org.bukkit.potion Potion PotionEffect PotionEffectType])
  (:import [org.bukkit.inventory ItemStack]))

(defn player-super-jump [evt player]
  (let [name (.getDisplayName player)]
    (when (= (.getType (.getItemInHand player)) Material/FEATHER)
        (let [amount (.getAmount (.getItemInHand player))
              x (if (.isSprinting player) (* amount 2) amount)
              x2 (/ (java.lang.Math/log x) 2) ]
          (c/consume-itemstack (.getInventory player) Material/FEATHER)
          (.setVelocity
            player
            (.add (org.bukkit.util.Vector. 0.0 x2 0.0) (.getVelocity player)))))))

(defn player-interact-event [evt]
  (let [player (.getPlayer evt)]
    (cond
      (and
        (= (.. player (getItemInHand) (getType)) Material/GLASS_BOTTLE)
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK)))
      (.setItemInHand player (.toItemStack (Potion. (rand-nth c/potion-types))  (rand-nth [1 1 2 3 5])))
      (and
        (= (.. player (getItemInHand) (getType)) Material/GOLD_SWORD)
        (= (.getHealth player) (.getMaxHealth player))
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/LEFT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/LEFT_CLICK_BLOCK)))
      (.throwSnowball player)
      (and
        (= (.. evt (getMaterial)) Material/MILK_BUCKET)
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK)))
      (do
        (.damage player 8)
        (.sendMessage player "you drunk milk"))
      (and
        (= (.. evt (getMaterial)) Material/FEATHER)
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK)))
      (player-super-jump evt player))))

(defonce swank* nil)
(defn on-enable [plugin]
  (when (nil? swank*)
    (def swank* (swank.swank/start-repl 4009))))
