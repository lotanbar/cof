# Circle of Fifths Trainer — High Level Design

## Platform & Stack
- **Platform:** Android
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Navigation:** Navigation Compose
- **No persistence** — the app is stateless (no database, no scores)

---

## Screens

### 1. Start Screen

**Layout:**
- A prominent **START** button at the top
- Three **mode checkboxes** below it:
  - Scales
  - Chords *(disabled/greyed out — not implemented)*
  - Circle
- Two **type checkboxes** below the mode checkboxes:
  - Major
  - Minor

**Behavior:**
- The START button is **disabled** until at least one mode checkbox is selected.
- If **Scales** is the only selected mode, at least one of Major/Minor must also be checked to enable START.
- If **Circle** is selected (alone or with Scales), Major/Minor do not affect START availability.
- Tapping START navigates to the Quiz Screen, passing the selected configuration.

Force high contrast dark theme throughut the app.

---

### 2. Quiz Screen

**Layout (always 3 sections):**

| Section | Height | Content |
|---|---|---|
| Top bar | 5% | Back button (left) · Question type label (center) |
| Middle | 75% | The question content |
| Bottom | 15% | Note selector buttons + Submit button |

**Top bar details:**
- Left side: a **back button** that returns to the Start Screen
- Center: plain text showing the active mode (e.g., `Scales · Major`, `Circle · Fifths`)

**Bottom note buttons:**
- Show all 12 chromatic notes ordered: `C · C#/Db · D · D#/Eb · E · F · F#/Gb · G · G#/Ab · A · A#/Bb · B`
- Enharmonic notes (sharps/flats) share a **single, slightly wider button** showing both names
- A **Submit** button sits below the note buttons

**Wrong answer behavior:**
- Display a "Wrong" indicator (e.g., brief toast or red flash)
- Stay on the same question
- Clear the current selection so the user can try again
- No hints are ever shown

**Correct answer behavior:**
- Advance immediately to the next randomized question

---

## Question Randomization Flow

Each new question is generated as follows:

1. **Randomize mode** — if more than one mode is enabled, pick one at random (Scales or Circle)
2. **Randomize type:**
   - If mode = **Scales**: pick Major or Minor at random (only from the ones the user checked)
   - If mode = **Circle**: pick Fourths or Fifths at random (always; independent of Major/Minor checkboxes)
3. **Randomize note** — pick any note from C to B (12 options) at random

---

## Question Types

### Scales Mode

**Top bar shows:** `Scales · Major` or `Scales · Minor`

**Middle section shows:** The root note letter large (e.g., `G`)

**User interaction:**
- Tap note buttons **in order** to build the full 7-note scale starting from the root
- Tapping the **last selected note again** undoes it (one-step undo only)
- Tap Submit to check the answer

**Correct answer:**
The 7 notes of the correct major or natural minor scale for that root, in ascending order starting from the root.

Examples:
- `G Major` → `G A B C D E F#`
- `D Minor` → `D E F G A Bb C`

---

### Chords Mode (TBD)
For both Scales/Chords mode the user must tap the notes in the correct order, which is why  the buttons should show small
numbers from 1 to 7 in their bottom left corner, signifyig the order in which the user tapped the buttons. 
---

### Circle Mode

**Top bar shows:** `Circle · Fourths` or `Circle · Fifths`

**Middle section shows:** The root note letter large, with a **left arrow (←)** for Fourths or **right arrow (→)** for Fifths

**User interaction:**
- Tap a single note button to select the answer
- Tap Submit to check

**Correct answer:**
The note that is a **perfect fourth above** (for Fourths) or **perfect fifth above** (for Fifths) the shown note. Wraps around chromatically (e.g., B + fifth = F#).

---

## Music Theory Reference (for implementation)

**Chromatic scale (12 notes):**
`C, C#/Db, D, D#/Eb, E, F, F#/Gb, G, G#/Ab, A, A#/Bb, B`

**Major scale formula:** W W H W W W H (whole/half steps)
**Natural minor scale formula:** W H W W H W W

**Perfect fifth = 7 semitones above (wrapping)**
**Perfect fourth = 5 semitones above (wrapping)**

---

## Out of Scope
- Chords mode
- Score tracking / streaks / high scores
- Sound playback

---

# Commit Plan

## Commit 1 — Scaffold + Start Screen
**What's built:**
- Android project initialized with Kotlin + Jetpack Compose + Navigation Compose
- Two navigation destinations defined: Start and Quiz (Quiz is a placeholder)
- Full Start Screen UI: mode checkboxes (Chords disabled), Major/Minor checkboxes, START button
- START button enable/disable logic wired to ViewModel state

**Testing scenarios:**
1. App launches and shows the Start Screen
2. START is disabled by default (nothing checked)
3. Checking "Circle" alone enables START
4. Checking "Scales" alone keeps START disabled until Major or Minor is also checked
5. Checking "Scales" + "Major" enables START
6. Tapping START navigates to the Quiz placeholder screen
7. Back gesture from Quiz returns to Start Screen

---

## Commit 2 — Quiz Screen Layout + Circle Mode
**What's built:**
- Full Quiz Screen layout: top bar (back button + label), middle section, bottom note buttons + submit
- All 12 note buttons rendered correctly; enharmonic notes (C#/Db etc.) are wider
- Circle mode question logic: randomize Fourths/Fifths, randomize note, display arrow
- Answer validation for Circle mode with wrap-around (e.g., B + fifth = F#)
- Wrong answer: "Wrong" feedback + selection cleared, same question shown
- Correct answer: next question generated

**Testing scenarios:**
1. Quiz screen shows correct 3-section layout
2. All 12 note buttons appear; C#/Db, D#/Eb, F#/Gb, G#/Ab, A#/Bb buttons are visibly wider
3. Top bar shows `Circle · Fourths` or `Circle · Fifths` correctly
4. Middle shows a note with the correct arrow direction
5. Selecting the correct fifth (e.g., G → D) and tapping Submit advances to a new question
6. Selecting the correct fourth (e.g., G → C) and tapping Submit advances
7. Correct answer wraps: B + fifth = F#, F + fourth = Bb
8. Wrong answer shows "Wrong", clears selection, stays on same question
9. Back button returns to Start Screen

---

## Commit 3 — Scales Mode
**What's built:**
- Scales mode question logic: randomize Major/Minor (from user selection), randomize root note
- Middle section shows the root note
- Note buttons support ordered multi-selection (tap to add to sequence)
- Tapping the last selected note again removes it (one-step undo)
- Answer validation: checks selected notes match the correct scale in correct order
- Wrong/correct behavior same as Circle mode

**Testing scenarios:**
1. Top bar shows `Scales · Major` or `Scales · Minor` correctly
2. Tapping note buttons appends them visually in order (e.g., a numbered or sequential highlight)
3. Tapping the last selected note deselects it
4. Tapping a non-last selected note does nothing
5. Correct answer: G Major → select G A B C D E F# in order → Submit → advances
6. Correct answer: D Minor → select D E F G A Bb C → Submit → advances
7. Wrong note order → Submit → "Wrong", cleared, same question
8. Wrong notes → Submit → "Wrong", cleared, same question
9. With only Major checked, all scale questions are Major
10. With only Minor checked, all scale questions are Minor

---

## Commit 4 — Multi-mode Randomization + Polish
**What's built:**
- Mode randomization when both Scales and Circle are checked
- Major/Minor randomization when both are checked (for Scales)
- UI polish: typography, spacing, colors, button states
- Edge case handling: enharmonic equivalence in answer checking (F# == Gb)

**Testing scenarios:**
1. With Scales + Circle both checked: questions alternate randomly between modes over ~20 questions
2. With Scales + Major + Minor checked: both major and minor questions appear over ~20 questions
3. With Circle only: no Major/Minor questions ever appear
4. Enharmonic equivalence: if the correct answer is F#, tapping the F#/Gb button is accepted
5. Full session: check all available options, go through ~30 questions with no crashes or repeated stuck states
6. Back button always returns to Start Screen cleanly
