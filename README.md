# Water-Light-Level

🌊**Water Light Level** is a **client-side Fabric mod** that visualizes **underwater blocks whose light level is below a configurable threshold**.

It renders a translucent **aura** around qualifying water blocks, making it easy to:
- spot **drowned-safe areas**
- identify **dark underwater regions**
- explore and debug **underwater lighting**

Available on:
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/water-light-level)
- [Modrinth](https://modrinth.com/mod/water-light-level)
- [GitHub](https://github.com/HasselAssel/Water-Light-Level/releases)

---

## ✨ Features

- 🔦 Highlights water blocks **below a chosen light level**
- 📏 Configurable **scan distance**
- 🎨 Customizable **aura color (ARGB)**
- ⌨ Toggle the aura on/off instantly
- ⌨️ Toggle the aura via a keystroke

---

![Example 1](assets/images/example1.png)

---

## 🧭 Commands

All commands are under:

`/waterlightlevel`<br>


### Light Level

`/waterlightlevel lightlevel <level>`<br>
`/waterlightlevel lightlevel`<br>

Sets or gets the **maximum light level** a water block may have to be highlighted.

---

### Scan Distance

`/waterlightlevel distance <distance>`<br>
`/waterlightlevel distance`<br>

Controls how far around the player the mod scans for water blocks.

---

### Aura Color (ARGB)

`/waterlightlevel color_argb <alpha><red><green><blue>`<br>
`/waterlightlevel color_argb`<br>

Customize the aura color and transparency. Value need to be in HEX.

---

### Toggle Aura

`/waterlightlevel on`<br>
`/waterlightlevel off`<br>
`/waterlightlevel toggle`<br>
or just use the toggle keybind (`L` by default)

Turns the aura **on / off** or **toggles** it.

---

### Aura Only

`/waterlightlevel aura_only`<br>

Scans the water once and renders the result. 
Good for large distances that would generate lag if continuously scanned.  
Updates on the waters light level will obviously not be taken into account when this setting is on.

---

![Example 2](assets/images/example2.png)

---

# Links

[Modrinth](https://modrinth.com/mod/water-light-level)

---

## 📦 Requirements

- **Minecraft**: 1.21.11  
- **Loader**: Fabric  
- **Fabric API**: required  
- **Client-side only** (safe to use on servers)

---

## 🧑‍💻 License

MIT
