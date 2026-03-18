# CampusAssist - Design Reference Card (Pocket Guide)

## 🎨 Color Palette

```
PRIMARY              SECONDARY            TERTIARY             NEUTRAL
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ #3F5F90      │    │ #555F71      │    │ #6F5675      │    │ #F9F9FF      │
│ BLUE         │    │ SLATE        │    │ PURPLE       │    │ BACKGROUND   │
│              │    │              │    │              │    │              │
│ Trust        │    │ Professional │    │ Luxury       │    │ Clean        │
│ Professional │    │ Supporting   │    │ Accent       │    │ Professional │
│ Education    │    │ Secondary    │    │ Highlight    │    │              │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘

TEXT COLORS
Dark Text:   #191C20  (On Surface, high contrast)
Gray Text:   #555F71  (On Surface Variant, secondary)
Light Text:  #F0F0F7  (On Surface, dark mode)

```

## 📐 Spacing Grid (8dp System)

```
8dp    ▯ Micro gap
16dp   ▯▯ Standard padding
24dp   ▯▯▯ Section gap
32dp   ▯▯▯▯ Large gap
48dp   ▯▯▯▯▯▯ Touch target min

Standard Padding Pattern:
┌─────────────────────────────────┐
│ 16dp  Content    Content  16dp   │ ← Sides: 16dp
│       ▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯     │
│       8dp gap between items    │ ← Rhythm: 8dp
│       ▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯▯     │
│ 16dp               16dp         │ ← Bottom: 16dp
└─────────────────────────────────┘
```

## 🎯 Elevation Levels

```
8dp  ████████  Logo card, modals
4dp  ████      Headers, cards
2dp  ██        Floating buttons
0dp  ▌         Flat surfaces
```

## 🔤 Typography Cheat Sheet

```
HEADLINES
28sp Bold    CampusAssist (app title)
20sp Bold    Browse Assignments (screens)
18sp Bold    Card titles

BODY TEXT
16sp Regular Primary content
13sp Regular Secondary content (metadata)
12sp Regular Labels, hints
11sp Regular Captions

COLOR RULES
On Surface            → Main text
On Surface Variant    → Secondary text
Primary               → Links, highlights
```

## 🎛️ Component Quick Guide

### Buttons

```
PRIMARY (Filled)
┌─────────────────────────────┐
│ Login                       │ ← 48dp height, rounded 24dp
│ Background: ?attr/colorPrimary      │
│ Text: ?attr/colorOnPrimary          │
└─────────────────────────────┘

SECONDARY (Outlined)
┌─────────────────────────────┐
│ View Details               │ ← 44dp height, rounded 20dp
│ Border: 1dp colorPrimary    │
│ Text: colorPrimary          │
└─────────────────────────────┘
```

### Chips

```
SELECTED (Filled)
╭─────────────╮
│ All      ● │ ← 32dp, colorPrimary bg
│ Rounded 20dp│
╰─────────────╯

UNSELECTED (Outlined)
╭─────────────╮
│ Math     ○ │ ← 32dp, outlined, colorSurfaceContainer
│ Rounded 20dp│
╰─────────────╯
```

### Cards

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃ Assignment Title   Status    ┃ ← 4dp elevation
┃                              ┃ ← 16dp radius
┃ Subject                      ┃ ← 16dp padding
┃                              ┃ ← colorSurfaceContainerLowest
┃  [ View Details ]            ┃ ← Material 3 button
┃ ← 12dp margin bottom        ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

### Input Fields

```
EMAIL ADDRESS
┌─────────────────────────────┐
│ you@university.edu    [?]   │ ← 48dp height
│ Background: colorSurfaceContainer  │
│ Border: colorOutline (focus)       │
│ Corner: 16dp          │
└─────────────────────────────┘
```

### Headers

```
┌─────────────────────────────┐
│ ⟨  Browse Assignments       │ ← 56dp height (?attr/actionBarSize)
│ 4dp elevation               │ ← colorPrimary background
│ 12dp start, 16dp end        │ ← colorOnPrimary text
└─────────────────────────────┘

Back Button Details:
- Size: 40dp (with 8dp padding = 56dp effective touch target)
- Icon: ic_menu_back
- Tint: ?attr/colorOnPrimary
- Background: ?attr/selectableItemBackgroundBorderless (ripple)
```

## 🎨 Common Patterns

### List Pattern
```
┌─────────────────────────────┐
│ [Header with elevation]     │ 4dp
├─────────────────────────────┤
│                             │
│ 16dp  ┏━━━━━━━━━━━━━━━━┓  │ 4dp
│      ┃ Card Item       ┃  │ elevation
│ 16dp ┃ 16dp padding    ┃16dp
│      ┗━━━━━━━━━━━━━━━━┛  │
│       12dp gap             │
│ 16dp  ┏━━━━━━━━━━━━━━━━┓  │
│      ┃ Card Item       ┃  │
│      ┗━━━━━━━━━━━━━━━━┛  │
│                             │
│ 16dp                   16dp│
└─────────────────────────────┘
```

### Form Pattern
```
┌──────────────────────────┐
│ 32dp margin top          │
│                          │
│ ┏━━━━━━━━━━━━━━━━━━━━┓  │
│ ┃ Logo (76dp, 8dp)   ┃  │ Premium feel
│ ┗━━━━━━━━━━━━━━━━━━━━┛  │
│ 20dp gap                 │
│                          │
│ CampusAssist (title)     │ 28sp bold
│ 8dp gap                  │
│ Marketplace (subtitle)   │ 13sp secondary
│ 32dp gap                 │
│                          │
│ ┌──────────────────────┐ │
│ │ Welcome Back (20dp)  │ │
│ │  ┌────┬───────┬────┐ │ │
│ │  │ S  │  W    │ S  │ │ │ Role selector
│ │  │udent│riter │ S  │ │ │
│ │  └────┴───────┴────┘ │ │
│ │ 16dp                  │ │
│ │ Email Label           │ │
│ │ 8dp gap               │ │
│ │ ┌──────────────────┐  │ │
│ │ │ Input field (48) │  │ │
│ │ └──────────────────┘  │ │
│ │ 16dp gap              │ │
│ │ Password Label        │ │
│ │ 8dp gap               │ │
│ │ ┌──────────────────┐  │ │
│ │ │ Input field (48) │  │ │
│ │ └──────────────────┘  │ │
│ │ 20dp gap              │ │
│ │ ┌──────────────────┐  │ │
│ │ │ Login Button (48) │ │
│ │ └──────────────────┘  │ │
│ │ 16dp gap              │ │
│ │ Don't have account?   │ │
│ │ [Register here]       │ │ (colorPrimary link)
│ │                       │ │
│ └──────────────────────┘ │
│ Card: colorSurfaceContainerLowest │
│ Elevation: 8dp, Radius: 20dp     │
│ 20dp margin top, bottom         │
│                          │
└──────────────────────────┘
```

## ⚡ Quick Reference

### Do's ✅
- ✅ Use theme attributes (?attr/...)
- ✅ Follow 8dp spacing rhythm
- ✅ Use Material 3 components
- ✅ Maintain touch target minimums (48dp)
- ✅ Use proper elevation (0, 2, 4, 8dp)
- ✅ Keep text contrast high
- ✅ Use proper corner radius (16dp, 20dp, 24dp)

### Don'ts ❌
- ❌ Hardcode colors (#XXXXXX)
- ❌ Use inconsistent spacing
- ❌ Use old MaterialComponents styles
- ❌ Make touch targets < 48dp
- ❌ Use harsh shadows
- ❌ Mix Material 2 and Material 3
- ❌ Use many different colors

## 🌙 Dark Mode

**Good news:** Dark mode is automatic!

Material 3 DayNight theme automatically adjusts:
- Primary color → lighter shade
- Background → dark
- Text → lighter
- All surfaces → darker

**No changes needed in XML** ✅

## 📱 Responsive Design

```
PHONE (320dp - 600dp)
┌─────────────────┐
│ [Content here]  │ Single column
│ 16dp padding    │
└─────────────────┘

TABLET (600dp+)
┌─────────────────────────────┐
│ [Content]    [Content]      │ Multi-column
│ 16dp padding, gap, 16dp     │
└─────────────────────────────┘
```

All uses `match_parent` width → responsive ✅

## 🔧 Common Tasks

### Change Primary Color
File: `app/src/main/res/values/colors.xml`
```xml
<color name="md_theme_primary">#3F5F90</color>
<!-- Change hex value -->
<!-- Entire app updates automatically -->
```

### Add New Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="48dp"
    app:cornerRadius="24dp"
    app:backgroundTint="?attr/colorPrimary"
    android:textColor="?attr/colorOnPrimary"
    android:text="Button Text"
    style="@style/Widget.Material3.Button" />
```

### Add New Card
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="?attr/colorSurfaceContainerLowest"
    android:layout_margin="16dp">
    <!-- Content here -->
</com.google.android.material.card.MaterialCardView>
```

## 📏 Dimensions at a Glance

| Element | Size | Notes |
|---------|------|-------|
| Header | 56dp | `?attr/actionBarSize` |
| Button | 48dp height | Minimum touch |
| Chip | 32dp height | Auto |
| Card | 16dp padding | Standard |
| Card corner | 16dp | Standard card |
| Button corner | 24dp | Primary |
| Button corner | 20dp | Secondary |
| Back button | 40dp | Touch target |
| Logo | 76dp | Premium |
| Divider | 1dp | Subtle |
| Elevation | 4dp | Standard |
| Elevation | 8dp | Premium |

---

## 🎓 Remember

> "Think 8dp, use theme attributes, follow Material 3, ship with confidence."

**You've got this!** 🚀

---

Last Updated: March 18, 2026  
Version: 1.0

