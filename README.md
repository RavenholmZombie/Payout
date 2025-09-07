<img src="https://iili.io/Ko2KRDv.png" width="150" alt="logo">

# Payout
## A simple mod that allows server admins to automatically give players items at definable intervals.

### Introduction
Payout is a simple yet highly-configurable mod that allows server owners and admins to routinely pay items to the server's playerbase.

## Configuring Payout
Payout's config is located at `world/serverconfig/payout-server.toml`.

### What you'll see when you first open the config.
````
[general]
	#The item to give players
	item = "minecraft:diamond"
	#How many items to give
	#Range: 1 ~ 64
	count = 1
	#Interval in milliseconds between payouts
	#Range: 1000 ~ 9223372036854775807
	intervalMillis = 60000
	#Sound event ID to play on payout
	payoutSound = "minecraft:entity.player.levelup"
	#Volume of payout sound
	#Range: 0.0 ~ 10.0
	payoutVolume = 1.0
	#Should payout play a sound?
	playPayoutSound = true
	#Should the server send a toast packet to connected clients during payout?
	showToast = true
	#What should the title of the toast say?
	toastTitle = "You got paid!"
	#What should the title of the toast say?
	toastDescription = "Thanks for playing!"
````
### Definitions
| Name              | Type     | Required | Description                                                                                   | Default Value                          | Example Value                |
|-------------------|----------|----------|-----------------------------------------------------------------------------------------------|----------------------------------------|------------------------------|
| `item`            | String   | No       | The item to give players. Must be a valid item ID.                                            | `"minecraft:diamond"`                   | `"minecraft:emerald"`        |
| `count`           | Integer  | No       | How many items to give. Range: **1 ~ 64**                                                     | `1`                                    | `5`                          |
| `intervalMillis`  | Long     | No       | Interval in **milliseconds** between payouts. Range: **1000 ~ 9223372036854775807**           | `60000` (60 seconds)                   | `120000` (2 minutes)         |
| `payoutSound`     | String   | No       | Sound event ID to play on payout. Must be a valid sound resource location.                    | `"minecraft:entity.player.levelup"`     | `"minecraft:entity.cat.purr"`|
| `payoutVolume`    | Float    | No       | Volume of the payout sound. Range: **0.0 ~ 10.0**                                             | `1.0`                                  | `0.5`                        |
| `playPayoutSound` | Boolean  | No       | Whether the payout should play a sound.                                                       | `true`                                 | `false`                      |
| `showToast`       | Boolean  | No       | Whether the server should send a toast packet to connected clients during payout.             | `true`                                 | `false`                      |
| `toastTitle`      | String   | No       | The title text of the toast shown on payout.                                                  | `"You got paid!"`                       | `"Daily Reward!"`            |
| `toastDescription`| String   | No       | The description text of the toast shown on payout.                                            | `"Thanks for playing!"`                 | `"Come back tomorrow!"`      |

## Commands
| Command                 | Syntax                                             | Description                                                                 |
|--------------------------|----------------------------------------------------|-----------------------------------------------------------------------------|
| **Set Reward Item**      | `/payout setItem <item> <quantity>`                | Sets the item and quantity given to players on each payout.                 |
| **Set Interval**         | `/payout setTime <h,m,d> <duration>`               | Sets the payout interval using hours (`h`), minutes (`m`), or days (`d`).   |
| **Debug Trigger**        | `/payout debug trigger`                            | Manually triggers a payout immediately.                                     |
| **Debug Reset**          | `/payout debug reset`                              | Resets the config back to defaults.                                         |
| **Debug Config Path**    | `/payout debug config`                             | Shows the current config file path and active values.                       |
| **Reload Config**        | `/payout reload`                                   | Reloads the `payout-server.toml` config file.                               |
| **Status**               | `/payout status`                                   | Shows current payout settings (reward, interval, time remaining, sounds, toasts). |
| **Set Payout Sound**     | `/payout setPayoutSound <sound_id> <volume>`       | Sets the sound event ID and volume for payout notifications.                |
| **Toggle Sound**         | `/payout setPlayPayoutSound <true,false>`          | Enables or disables payout sound playback.                                  |
| **Toggle Toasts**        | `/payout setShowToasts <true,false>`               | Enables or disables showing toast notifications to clients.                 |
| **Set Toast Title**      | `/payout setToastTitle <text>`                     | Sets the title text of the toast notification.                              |
| **Set Toast Description**| `/payout setToastDescription <text>`               | Sets the description text of the toast notification.                        |

