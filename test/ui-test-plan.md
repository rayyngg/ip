# TryBot UI test plan

This file defines the scripted console UI tests used by the `test-ui` project skill.

## Test-session information

- Working directory: repository root
- Java requirement: Java 25
- Setup command: `javac -d out src/main/java/*.java`
- Program command: `java -cp out TryBot`
- Output comparison: exact, with CRLF normalized to LF and the final newline treated as optional
- Execution order: top to bottom; positive and negative cases are intentionally interleaved; stop immediately after the first failure

## Test cases

### UI-001 — Start TryBot and exit (positive)

- Aim: Verify that TryBot starts with the expected welcome screen and exits when the user enters `bye`.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-002 — Reject invalid commands without adding tasks (negative)

- Aim: Verify that a missing todo description and an unknown command show errors, while `list` confirms that no task was added.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
  todo
  blah
  list
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  A todo needs a description. Try: todo read book.
  ____________________________________________________________
  ____________________________________________________________
  I do not recognise that command. Try todo, list, or bye.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-003 — Complete a todo lifecycle (positive)

- Aim: Verify that a valid todo can be added, marked done, listed as done, unmarked, and listed as not done again.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
  todo write report
  mark 1
  list
  unmark 1
  list
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
  [T][ ] write report
  Now you have 1 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Good work!! I've marked this task as done:
  [T][X] write report
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][X] write report
  ____________________________________________________________
  ____________________________________________________________
  OK, I've marked this task as not done yet:
  [T][ ] write report
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][ ] write report
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-004 — Reject malformed structured commands (negative)

- Aim: Verify that malformed deadline, event, and mark commands produce actionable messages and leave the task list empty.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
  deadline report
  event planning
  mark
  list
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  A deadline needs /by followed by a date or time. Example: deadline report /by Friday.
  ____________________________________________________________
  ____________________________________________________________
  An event needs /from and /to time details. Example: event meeting /from Monday /to Tuesday.
  ____________________________________________________________
  ____________________________________________________________
  Mark needs one task number. Example: mark 1.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-005 — Add deadline and event tasks (positive)

- Aim: Verify that valid deadline and event inputs create the correct task types and preserve their details in the list.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
  deadline submit report /by Friday
  event team meeting /from Monday /to Tuesday
  list
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
  [D][ ] submit report (by: Friday)
  Now you have 1 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
  [E][ ] team meeting (from: Monday to: Tuesday)
  Now you have 2 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[D][ ] submit report (by: Friday)
  2.[E][ ] team meeting (from: Monday to: Tuesday)
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-006 — Preserve valid state after invalid inputs (negative)

- Aim: Verify that malformed deadline/event commands and invalid mark/unmark arguments do not alter an already valid todo.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
  todo valid task
  deadline incomplete
  event incomplete
  mark abc
  unmark 2
  list
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
  [T][ ] valid task
  Now you have 1 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  A deadline needs /by followed by a date or time. Example: deadline report /by Friday.
  ____________________________________________________________
  ____________________________________________________________
  An event needs /from and /to time details. Example: event meeting /from Monday /to Tuesday.
  ____________________________________________________________
  ____________________________________________________________
  The task number must be a whole number. Example: mark 1.
  ____________________________________________________________
  ____________________________________________________________
  That task number does not exist. Use list to see your task numbers.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][ ] valid task
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-007 — Accept whitespace and case variations (positive)

- Aim: Verify that command matching is case-insensitive, surrounding whitespace is ignored, and the todo description is trimmed.
- Command: `java -cp out TryBot`
- Inputs:

  ```text
    ToDo:   buy milk  
  LiSt
  BYE!
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
  [T][ ] buy milk
  Now you have 1 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][ ] buy milk
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

### UI-008 — Reject empty and boundary inputs (negative)

- Aim: Verify that blank input, zero-based task numbers, out-of-range task numbers, and an empty `todo:` do not create a task.
- Command: `java -cp out TryBot`
- Inputs:

  ```text

  mark 0
  unmark 999
  todo:
  list
  bye
  ```

- Expected output:

  ```text
  ____________________________________________________________
   _____             ____        _
  |_   _| _ __ _   _ | __ )  ___ | |_
    | |  | '__| | | ||  _ \ / _ \| __|
    | |  | |  | |_| || |_) | (_) | |_
    |_|  |_|   \__, ||____/ \___/ \__|
                |___/
  Hello! I'm TryBot.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  I need a command. Try todo, list, or bye.
  ____________________________________________________________
  ____________________________________________________________
  That task number does not exist. Use list to see your task numbers.
  ____________________________________________________________
  ____________________________________________________________
  That task number does not exist. Use list to see your task numbers.
  ____________________________________________________________
  ____________________________________________________________
  A todo needs a description. Try: todo read book.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
  Bye. Hope to see you again soon!
  ____________________________________________________________
  ```
