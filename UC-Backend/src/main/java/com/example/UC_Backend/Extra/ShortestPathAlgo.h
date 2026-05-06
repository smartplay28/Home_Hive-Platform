int findShortestPath(int source, int destination, vector<vector<Edge> > &adjList, int n)
{
    vector<int> dist(n, INT_MAX);
    dist[source] = 0;

    priority_queue<Edge, vector<Edge>, greater<Edge> > pq;
    pq.push(Edge(0, source));

    vector<int> parent(n, -1);

    while (!pq.empty())
    {
        int currentDist = pq.top().first;
        int currentNode = pq.top().second;
        pq.pop();

        if (currentDist > dist[currentNode])
            continue;

        for (const auto &neighbor : adjList[currentNode])
        {
            int nextNode = neighbor.first;
            int weight = neighbor.second;

            if (dist[currentNode] + weight < dist[nextNode])
            {
                dist[nextNode] = dist[currentNode] + weight;
                pq.push(Edge(dist[nextNode], nextNode));
                parent[nextNode] = currentNode;
            }
        }
    }

    if (dist[destination] == INT_MAX)
    {
        return -1;
    }
    else
    {
        return dist[destination];
    }
}