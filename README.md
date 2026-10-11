# Nether API

---

### Info For Mod Devs

##### Add the following to your `build.gradle` to add this mod's files to your workspace:

```groovy
dependencies {
    deobfCompile 'com.github.jbredwards:nether-API:1.12.2-SNAPSHOT'
}

repositories {
    maven { url 'https://jitpack.io' }
}
```

##### Any mods using this as a dependancy must make sure they're using stable_39 mappings for this mod to work properly in a deobfuscated enviornment!

---

## ambience.json

A new config file introduced in v1.5.0 that lets players set custom biome ambience properties!

The format of ambience.json is:

<img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> The root object, where each key is a biome id.
>
> <img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **Biome ID**: An entry (see below).

##### Entry

Specifies biome ambience for the given biome, as well as nether/end fog color.

The format of an entry is:

<img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> The root object.
>
> <img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **ambience**: The biome ambience object.
>
> > <img src="https://minecraft.wiki/images/NbtSprite_list.png" title="JSON Array" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **particles**: The ambient particles.
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> A particle.
> > >
> > > > <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **id**: Required. Name of the particle to use, e.g. `explode`.
> > >
> > > > <img src="https://minecraft.wiki/images/NbtSprite_float.png" title="Float" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **chance**: Fractional chance of the particle appearing. Between 0 and 1.
> > >
> > > > <img src="https://minecraft.wiki/images/NbtSprite_list.png" title="Int Array" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **params**: List of Int parameters passed into the particle at creation. Some particles may require this, but most don't.
> >
> > <img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **passive_sound**: The ambient sound (loop sound).
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **id**: Required. [Resource location](https://minecraft.wiki/w/Identifier) of the sound, e.g. `minecraft:entity.snowball.throw`.
> >
> > <img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **random_sound**: The random ambient sound (loop additions sound).
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **id**: Required. [Resource location](https://minecraft.wiki/w/Identifier) of the sound.
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_float.png" title="Float" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **chance**: Fractional chance of the sound playing. Between 0 and 1. In most cases this should be left at its default value.
> >
> > <img src="https://minecraft.wiki/images/NbtSprite_compound.png" title="JSON Object" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **dark_sound**: The dark ambience sound (mood sound).
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **id**: Required. [Resource location](https://minecraft.wiki/w/Identifier) of the sound.
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_float.png" title="Float" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **chance**: Fractional chance of the sound playing. Between 0 and 1. In most cases this should be left at its default value.
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_int.png" title="Float" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **light_search_radius**: The radius (in blocks) that will have its light levels checked. In most cases this should be left at its default value.
> >
> > > <img src="https://minecraft.wiki/images/NbtSprite_float.png" title="Float" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **sound_offset**: Distance away from the player to play the sound. In most cases this should be left at its default value.
> 
> <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> <img src="https://minecraft.wiki/images/NbtSprite_int.png" title="Int" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **end_fog_color**: The biome fog color if in the End. Accepts a hexadecimal String if prefixed with "#" or "0x".
>
> <img src="https://minecraft.wiki/images/NbtSprite_string.png" title="String" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> <img src="https://minecraft.wiki/images/NbtSprite_int.png" title="Int" align="absmiddle" style="width:1em; height:1em; border-radius:0;"> **nether_fog_color**: The biome fog color if in the Nether. Accepts a hexadecimal String if prefixed with "#" or "0x".

### Example of an ambience.json file:

```json
{
  "netherex:arctic_abyss": {
    "ambience": {
      "particles": [{
        "id": "fallingdust",
        "chance": 0.002,
        "params": [ 80 ]
      }]
    },
    "nether_fog_color": "#7A7A9F"
  },
  "netherex:ruthless_sands": {
    "nether_fog_color": "#816A5A"
  }
}
```
