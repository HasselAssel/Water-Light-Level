# Water-Light-Level

🌊**Water Light Level** is a **client-side Fabric mod** that visualizes **underwater blocks whose light level is below a configurable threshold**.

It renders a translucent **aura** around qualifying water blocks, making it easy to:
- spot **drowned-safe areas**
- identify **dark underwater regions**
- explore and debug **underwater lighting**

---

## ✨ Features

- 🔦 Highlights water blocks **below a chosen light level**
- 📏 Configurable **scan distance**
- 🎨 Customizable **aura color (ARGB)**
- ⌨ Toggle the aura on/off instantly

---

## 🧭 Commands

All commands are under:

`/waterlightlevel`


### Light Level

`/waterlightlevel setlight <level>`
`/waterlightlevel getlight`

Sets or gets the **maximum light level** a water block may have to be highlighted.

---

### Scan Distance

`/waterlightlevel setdist <distance>`
`/waterlightlevel getdist`

Controls how far around the player the mod scans for water blocks.

---

### Aura Color (ARGB)

`/waterlightlevel setargb <alpha> <red> <green> <blue>`
`/waterlightlevel getargb`

Customize the aura color and transparency. Values need to be in HEX.

---

### Toggle

`/waterlightlevel toggle`

Turns the aura **on or off** without changing any settings.

---

## 📦 Requirements

- **Minecraft**: 1.21.11  
- **Loader**: Fabric  
- **Fabric API**: required  
- **Client-side only** (safe to use on servers)

---

## 🧑‍💻 License

CC0-1.0 — do whatever you want 🙂
