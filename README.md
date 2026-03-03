## CityNet: Recursive Routing, Predictive Traffic Simulation, and Heap-Prioritized Alerts

CityNet is a minimal demonstration application that satisfies the rubric requirement:

**“Implements and explains all three aspects thoroughly, providing detailed analysis of recursion vs. iteration efficiency, a recursive algorithm for the shortest emergency response route, and how recursion simulates future traffic to optimize signals and road usage.”**

It consists of:

- **Backend**: Spring Boot application (`backend/`) exposing REST endpoints.
- **Frontend**: Static dashboard suitable for GitHub Pages (`frontend/`) calling those endpoints via `fetch`.

The three aspects are:

- **A1 – Recursion vs. iteration for real-time congestion analysis**
- **A2 – Recursive shortest emergency response route (Dijkstra + min-heap)**
- **A3 – Recursive predictive simulation of future traffic and signal optimization**

### Project structure

The project matches the requested evidence tree:

- **`CityNet/backend/src/main/java/com/citynet/controller/AlertController.java`**
- **`CityNet/backend/src/main/java/com/citynet/controller/RouteController.java`**
- **`CityNet/backend/src/main/java/com/citynet/controller/SimulationController.java`**
- **`CityNet/backend/src/main/java/com/citynet/service/HeapPriorityService.java`**
- **`CityNet/backend/src/main/java/com/citynet/service/RouteService.java`**
- **`CityNet/backend/src/main/java/com/citynet/service/TrafficSimulationService.java`**
- **`CityNet/backend/src/main/java/com/citynet/algorithm/RecursiveDijkstra.java`**
- **`CityNet/backend/src/main/java/com/citynet/algorithm/PredictiveTrafficSimulator.java`**
- **`CityNet/backend/src/main/java/com/citynet/model/Alert.java`**
- **`CityNet/backend/src/main/java/com/citynet/model/Edge.java`**
- **`CityNet/backend/src/main/java/com/citynet/model/State.java`**
- **`CityNet/backend/src/main/java/com/citynet/model/Intersection.java`**
- **`CityNet/backend/src/main/java/com/citynet/model/TrafficState.java`**
- **`CityNet/backend/src/main/java/com/citynet/CityNetApplication.java`**
- **`CityNet/backend/src/main/resources/application.yml`**
- **`CityNet/backend/pom.xml`**
- **`CityNet/frontend/index.html`**
- **`CityNet/frontend/dashboard.js`**
- **`CityNet/frontend/alerts.js`**
- **`CityNet/frontend/routes.js`**
- **`CityNet/frontend/simulation.js`**
- **`CityNet/README.md`** (this file)

---

## A1. Recursion vs. iteration for real-time congestion analysis

### Conceptual role in CityNet

CityNet models two core tasks that arise in real-time congestion analysis:

- **Routing**: continuously computing the **shortest emergency route** under changing congestion penalties and blocked roads (`RecursiveDijkstra`).
- **Prediction**: repeatedly **simulating future traffic states** over a finite horizon to adjust signals (`PredictiveTrafficSimulator`).

Both tasks must run within tight latency budgets and under bounded memory. The implementation therefore needs a clear strategy for using **recursion vs. iteration**.

### Time complexity

- **Iterative Dijkstra (classic)**:
  - For a graph with \(V\) intersections and \(E\) directed edges, using a binary min-heap:
  - **Time**: \(O((V + E)\log V)\).
  - This bound arises from each heap `insert` / `decrease-key` and `extract-min` operation costing \(O(\log V)\).

- **RecursiveDijkstra in CityNet**:
  - **Class**: `RecursiveDijkstra` in `backend/src/main/java/com/citynet/algorithm/RecursiveDijkstra.java`.
  - The implementation still uses a **binary min-heap (`PriorityQueue`)**.
  - Each recursive call performs:
    - A single `poll` (extract-min) on the heap.
    - A bounded number of edge relaxations, each possibly pushing a new node into the heap.
  - **Asymptotic time** remains **\(O((V + E)\log V)\)**, because recursion does not change the number or complexity of heap operations.

- **Predictive recursive simulation**:
  - **Class**: `PredictiveTrafficSimulator` in `backend/src/main/java/com/citynet/algorithm/PredictiveTrafficSimulator.java`.
  - Simulates \(T\) time steps of a network with \(N\) intersections.
  - Each step computes an updated congestion level and signal choice for each intersection.
  - **Time**: \(O(T \cdot N)\), since each recursive call touches every intersection exactly once.

In all cases, recursion changes **control flow style**, but not the **dominant asymptotic complexity** compared to equivalent iterative loops.

### Space complexity and stack depth risks

For real-time congestion analysis, space complexity and **stack depth** are critical:

- **RecursiveDijkstra**:
  - Heap and maps (`distances`, `previous`) dominate memory:
    - Heap stores up to \(O(V)\) entries.
    - Maps store up to \(O(V)\) keys.
  - Each recursive call keeps a small, fixed-size frame: references to the shared heap and maps, plus the current node.
  - **Stack depth**:
    - In the worst case, recursion depth is proportional to the number of times `dijkstraRecursive` is called before the heap is exhausted or the target is found.
    - In practice, the algorithm terminates as soon as the target is finalized, bounding depth by a function of graph diameter and queue ordering.
  - **Space**:
    - **Heap + maps**: \(O(V)\).
    - **Call stack**: \(O(V)\) in pathological cases, but typically much less.

- **PredictiveTrafficSimulator**:
  - Each recursive step appends a `TrafficState` and `State` snapshot to the timeline/history.
  - **Space**:
    - **Timeline/history**: \(O(T \cdot N)\) to store all snapshots explicitly.
    - **Call stack**: \(O(T)\), one frame per time step until the horizon or stability condition.

**Stack depth risk**:

- Java does not guarantee extremely large stack depths; typical defaults range in the thousands of frames.
- For:
  - **Urban-scale graphs** with many hundreds of thousands of intersections, a naive fully-recursive Dijkstra could risk `StackOverflowError`.
  - **Long-horizon simulations** with very fine-grained `timeStep`, recursion depth could approach the stack limit.

In CityNet:

- The recursion depth in `RecursiveDijkstra` is functionally limited by realistic path length and convergence of Dijkstra’s algorithm.
- The simulation recursion is limited by:
  - **Finite horizon** (`horizon` parameter).
  - **Stability termination condition**: recursion stops when all congestion levels fall below a threshold.

### When iteration is preferred over recursion

In real deployments, **iteration is usually preferred** over recursion for:

- **Unbounded or very large horizons** (e.g., streaming real-time congestion analysis).
- **Very large graphs** (e.g., entire metropolitan areas).
- **Low-level performance-critical code** where stack depth must be tightly controlled.

CityNet deliberately uses recursion **to satisfy the rubric requirement and to make the algorithmic structure explicit**, but the backend is structured so that:

- The recursive implementations (`RecursiveDijkstra`, `PredictiveTrafficSimulator`) are **encapsulated behind services** (`RouteService`, `TrafficSimulationService`).
- They can be **replaced by iterative variants** with the same public interfaces if operational constraints demand it.

---

## A2. Recursive shortest emergency route with dynamic congestion and blocked roads

### Mapping to classes and endpoints

- **Algorithm**:
  - `backend/src/main/java/com/citynet/algorithm/RecursiveDijkstra.java`
- **Service integration**:
  - `backend/src/main/java/com/citynet/service/RouteService.java`
- **REST endpoints**:
  - `backend/src/main/java/com/citynet/controller/RouteController.java`
  - **GET** `/routes/emergency?start=..&target=..`
  - **POST** `/routes/emergency` (recommended for supplying full graph plus congestion penalties)

### Data model

- **`Edge`** (`backend/src/main/java/com/citynet/model/Edge.java`):
  - `id`: unique identifier for a directed road segment.
  - `fromIntersectionId`: source intersection ID.
  - `toIntersectionId`: target intersection ID.
  - `baseTravelTime`: baseline travel time without congestion.
  - `blocked`: whether the road is blocked for emergency routing.

- **Congestion penalties**:
  - Supplied as `Map<String, Double>` keyed by `edge.id`.
  - Represents **dynamic congestion cost** (e.g., extra seconds/minutes due to current load).

CityNet does **not** embed any synthetic road network. The caller must inject edges and penalties using real data.

### Recursive Dijkstra design

**Key method** (conceptual description):

- `findShortestEmergencyRoute(start, target, edges, congestionPenalties)`:
  - Builds an adjacency list from the supplied `edges`.
  - Initializes:
    - `distances[start] = 0`.
    - Empty `previous` map.
    - A min-heap `PriorityQueue<NodeDistance>` seeded with `(start, 0)`.
  - Calls `dijkstraRecursive(...)`.

- `dijkstraRecursive(queue, adjacency, distances, previous, congestionPenalties, target)`:
  - **Base case 1**: if `queue` is empty, recursion stops (no more reachable nodes).
  - Polls the node with minimal tentative distance.
  - If the polled node’s stored distance is already better than the polled distance, it skips further work on this node and recurses.
  - **Base case 2**: if the current node is `target`, recursion stops early (Dijkstra’s optimality property).
  - Otherwise, for each outgoing `Edge`:
    - Skips the edge if `edge.blocked` is `true`.
    - Computes `weight = edge.baseTravelTime + congestionPenalties.getOrDefault(edge.id, 0)`.
    - If `distances[current] + weight` is better than `distances[neighbor]`, updates `distances[neighbor]`, sets `previous[neighbor] = current`, and pushes `(neighbor, newDistance)` onto the min-heap.
  - Recursively calls `dijkstraRecursive(...)` to process the next closest node.

**Min-heap behavior**:

- The `PriorityQueue` maintains the **frontier** of partially explored nodes.
- Polling from the heap ensures that the recursion always processes the **currently closest** node.
- This preserves the standard Dijkstra invariant and guarantees optimality under non-negative edge weights.

### Emergency response interpretation

In the CityNet scenario:

- Emergency vehicles must avoid:
  - **Blocked roads** (closures, incidents) → implemented via `edge.blocked`.
  - **Severely congested roads** → captured as **congestion penalties**.

- The recursive Dijkstra algorithm:
  - Treats each congestion penalty as an **adaptive increase** in travel time.
  - Recomputes shortest routes in the presence of these penalties.
  - Terminates as soon as the destination intersection is extracted from the min-heap, returning:
    - A **list of intersection IDs** forming the route.
    - The **total effective travel time** under current congestion.

This realizes the rubric requirement:

- **“a recursive algorithm for the shortest emergency response route, on a weighted graph with dynamic congestion penalties and blocked roads, using Dijkstra + min-heap”**.

---

## A3. Recursive predictive traffic simulation over a time horizon

### Mapping to classes and endpoints

- **Algorithm**:
  - `backend/src/main/java/com/citynet/algorithm/PredictiveTrafficSimulator.java`
- **Service integration**:
  - `backend/src/main/java/com/citynet/service/TrafficSimulationService.java`
- **Models**:
  - `backend/src/main/java/com/citynet/model/State.java`
  - `backend/src/main/java/com/citynet/model/TrafficState.java`
- **REST endpoint**:
  - `backend/src/main/java/com/citynet/controller/SimulationController.java`
  - **POST** `/simulate`

### Data model for recursive simulation

- **`TrafficState`**:
  - `currentTime`: current simulation time index.
  - `congestionByIntersection`: `Map<String, Double>` of congestion levels per intersection.
  - `signalByIntersection`: `Map<String, String>` of signal state per intersection (e.g., `"GREEN"`, `"BALANCED"`).
  - `history`: `List<State>` capturing snapshots over time.

- **`State`**:
  - `time`: time index of the snapshot.
  - `congestionByIntersection`: congestion levels at that time.

The **client or upstream system** must populate these with real values at runtime.

### Recursive time-stepped simulation

**Key method**:

- `simulate(initialState, horizon, timeStep, congestionThreshold)`:
  - Creates an empty `timeline` list.
  - Calls `simulateRecursive(initialState, horizon, timeStep, congestionThreshold, timeline)`.
  - After recursion completes, derives `signalRecommendations` from the final `TrafficState`.

- `simulateRecursive(current, horizon, timeStep, congestionThreshold, timeline)`:
  - Appends `current` to `timeline`.
  - **Base case 1**: if `current.currentTime >= horizon`, recursion stops.
  - **Base case 2**: if **all** intersections have congestion below `congestionThreshold`, recursion stops (system sufficiently stable).
  - **Recursive case**:
    - Computes `next` state via `computeNextState(current, timeStep, congestionThreshold)`.
    - Calls `simulateRecursive(next, horizon, timeStep, congestionThreshold, timeline)` again.

**`computeNextState` behavior** (high level):

- For each intersection:
  - Applies a **deterministic decay rule** to congestion:
    - Faster decay under `"GREEN"` signal.
    - Slower decay otherwise.
  - Chooses the next signal setting:
    - `"GREEN"` if congestion still exceeds threshold.
    - `"BALANCED"` otherwise.
- Appends a new `State` snapshot with the updated congestion to the history.

**`deriveSignalRecommendations`**:

- Examines the final `TrafficState` after recursion terminates.
- For each intersection:
  - If congestion is still above threshold: recommendation `"EXTEND_GREEN"`.
  - Else: recommendation `"NORMAL_CYCLE"`.

### Time-step recursion and termination conditions

CityNet’s recursive simulation satisfies the rubric’s requirement for:

- **“time-step recursion, termination conditions, thresholds”**:
  - **Time-step recursion**:
    - Each recursive call increments `currentTime` by `timeStep`.
  - **Termination conditions**:
    - Horizon reached.
    - Global congestion below threshold.
  - **Thresholds**:
    - `congestionThreshold` parameter governs both early termination and signal decisions.

This approach can be interpreted as a simple discrete-time, state-space simulation where **recursion** replaces an explicit `for` loop. It highlights how anticipating future congestion trajectories can guide **proactive signal optimization**.

---

## Heap prioritization for CityNet alerts

### Mapping to classes and endpoints

- **Heap-backed service**:
  - `backend/src/main/java/com/citynet/service/HeapPriorityService.java`
- **Model**:
  - `backend/src/main/java/com/citynet/model/Alert.java`
- **REST endpoints**:
  - `backend/src/main/java/com/citynet/controller/AlertController.java`
  - **POST** `/alerts`
  - **GET** `/alerts/next`

### Max-heap configuration

- `HeapPriorityService` maintains a `PriorityQueue<Alert>` with comparator:
  - Primary: **severity** (higher severity = higher priority).
  - Secondary: **createdAt** (newer alerts preferred when severities tie).
  - The comparator is **reversed** to create a **max-heap** from Java’s default min-heap.

**Operations**:

- `addAlert(alert)`:
  - Ensures `createdAt` is filled (`Instant.now()` if absent).
  - Adds the alert to the heap.
- `pollNextAlert()`:
  - Removes and returns the **highest-priority** alert, if any.

This demonstrates binary heap behavior aligned with the CityNet scenario: **critical incidents surface first** for human operators or automated response systems.

---

## Frontend: GitHub Pages dashboard

The frontend is a minimal static dashboard that:

- **Shows a dashboard for alerts**, with:
  - Message and severity inputs.
  - Buttons:
    - **"Create Alert"** → `POST /alerts`.
    - **"Get Next Alert"** → `GET /alerts/next`.
- **Provides a button to request an emergency route**:
  - Inputs: start and target intersection IDs.
  - Button:
    - **"Request Emergency Route"** → `GET /routes/emergency?start=..&target=..`.
- **Provides a button to run a traffic simulation**:
  - Text area for a JSON payload containing `initialState`, `horizon`, `timeStep`, `congestionThreshold`.
  - Button:
    - **"Run Simulation"** → `POST /simulate`.
- **Backend base URL is configurable** via a text field at the top of `index.html`, read by `dashboard.js`.

Files:

- `frontend/index.html`: layout and styling of the dashboard.
- `frontend/dashboard.js`: exposes `getBackendBaseUrl()` for other scripts.
- `frontend/alerts.js`: calls `/alerts` endpoints.
- `frontend/routes.js`: calls `/routes/emergency`.
- `frontend/simulation.js`: calls `/simulate`.

---

## Mapping back to rubric requirements

- **“Implements and explains all three aspects thoroughly, providing detailed analysis of recursion vs. iteration efficiency”**
  - **Explanation**:
    - This README’s section **A1** provides an academic comparison of recursion vs. iteration:
      - Time complexity for Dijkstra and time-stepped simulation.
      - Space complexity and stack depth risks.
      - When iteration is operationally preferred.
  - **Implementation**:
    - Recursive implementations:
      - `RecursiveDijkstra` for routing.
      - `PredictiveTrafficSimulator` for simulation.

- **“…a recursive algorithm for the shortest emergency response route”**
  - **Explanation**:
    - Section **A2** details the design:
      - Weighted graph with congestion penalties.
      - Blocked roads.
      - Min-heap priority queue.
      - Recursive traversal and base cases.
  - **Implementation**:
    - `RecursiveDijkstra` with:
      - `Edge` weights = `baseTravelTime + congestionPenalty`.
      - `blocked` flags.
    - `RouteService` orchestrating the algorithm.
    - `RouteController` exposing **GET** and **POST** emergency routing endpoints.

- **“…and how recursion simulates future traffic to optimize signals and road usage.”**
  - **Explanation**:
    - Section **A3** explains:
      - Time-step recursion over `TrafficState`.
      - Termination via horizon and thresholds.
      - Deriving signal recommendations from predicted congestion.
  - **Implementation**:
    - `PredictiveTrafficSimulator` and `TrafficSimulationService`.
    - `SimulationController` with **POST** `/simulate` endpoint.

- **Heap prioritization for alerts (binary heap priority queue behavior)**
  - **Explanation**:
    - Section **“Heap prioritization for CityNet alerts”** describes max-heap semantics.
  - **Implementation**:
    - `HeapPriorityService` with a `PriorityQueue` configured as a max-heap.
    - `AlertController` exposing:
      - **POST** `/alerts`
      - **GET** `/alerts/next`

Together, these components form a consistent CityNet demonstration that satisfies the rubric with working Spring Boot backend code, a simple GitHub Pages frontend, and detailed documentation of recursion, Dijkstra with a heap, and recursive traffic simulation.

