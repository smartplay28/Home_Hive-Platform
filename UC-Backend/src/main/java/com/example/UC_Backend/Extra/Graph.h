#include <vector>
using namespace std;

typedef pair<int, int> Edge;

void initializeGraph(vector<vector<Edge> > &adjList)
{
    vector<Edge> edges;
    edges.push_back(Edge(1, 8));
    edges.push_back(Edge(6, 7));
    edges.push_back(Edge(11, 4));
    edges.push_back(Edge(24, 10));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(0, 8));
    edges.push_back(Edge(2, 3));
    edges.push_back(Edge(3, 4));
    edges.push_back(Edge(6, 8));
    edges.push_back(Edge(7, 2));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(1, 3));
    edges.push_back(Edge(3, 1));
    edges.push_back(Edge(7, 5));
    edges.push_back(Edge(13, 4));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(1, 4));
    edges.push_back(Edge(2, 1));
    edges.push_back(Edge(4, 7));
    edges.push_back(Edge(8, 4));
    edges.push_back(Edge(9, 1));
    edges.push_back(Edge(25, 4));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(3, 7));
    edges.push_back(Edge(5, 8));
    edges.push_back(Edge(9, 3));
    edges.push_back(Edge(10, 2));
    edges.push_back(Edge(26, 9));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(4, 8));
    edges.push_back(Edge(10, 2));
    edges.push_back(Edge(16, 3));
    edges.push_back(Edge(22, 9));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(0, 7));
    edges.push_back(Edge(1, 8));
    edges.push_back(Edge(7, 4));
    edges.push_back(Edge(11, 3));
    edges.push_back(Edge(13, 8));
    edges.push_back(Edge(16, 5));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(1, 2));
    edges.push_back(Edge(2, 5));
    edges.push_back(Edge(6, 4));
    edges.push_back(Edge(4, 6));
    edges.push_back(Edge(8, 3));
    edges.push_back(Edge(12, 2));
    edges.push_back(Edge(13, 4));
    edges.push_back(Edge(21, 8));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(3, 4));
    edges.push_back(Edge(7, 3));
    edges.push_back(Edge(9, 4));
    edges.push_back(Edge(14, 4));
    edges.push_back(Edge(24, 4));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(3, 1));
    edges.push_back(Edge(4, 3));
    edges.push_back(Edge(8, 4));
    edges.push_back(Edge(10, 2));
    edges.push_back(Edge(14, 2));
    edges.push_back(Edge(15, 2));
    edges.push_back(Edge(21, 8));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(4, 2));
    edges.push_back(Edge(5, 2));
    edges.push_back(Edge(9, 2));
    edges.push_back(Edge(12, 4));
    edges.push_back(Edge(16, 3));
    edges.push_back(Edge(21, 3));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(0, 4));
    edges.push_back(Edge(6, 3));
    edges.push_back(Edge(12, 8));
    edges.push_back(Edge(22, 6));
    edges.push_back(Edge(23, 3));
    edges.push_back(Edge(25, 3));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(7, 2));
    edges.push_back(Edge(10, 4));
    edges.push_back(Edge(11, 8));
    edges.push_back(Edge(17, 2));
    edges.push_back(Edge(18, 3));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(2, 4));
    edges.push_back(Edge(6, 8));
    edges.push_back(Edge(7, 4));
    edges.push_back(Edge(20, 8));
    edges.push_back(Edge(24, 2));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(8, 4));
    edges.push_back(Edge(9, 2));
    edges.push_back(Edge(19, 6));
    edges.push_back(Edge(20, 3));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(9, 2));
    edges.push_back(Edge(19, 4));
    edges.push_back(Edge(25, 6));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(5, 3));
    edges.push_back(Edge(6, 5));
    edges.push_back(Edge(10, 3));
    edges.push_back(Edge(21, 6));
    edges.push_back(Edge(22, 8));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(12, 2));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(12, 3));
    edges.push_back(Edge(19, 8));
    edges.push_back(Edge(23, 3));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(14, 6));
    edges.push_back(Edge(15, 4));
    edges.push_back(Edge(18, 8));
    edges.push_back(Edge(25, 1));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(13, 8));
    edges.push_back(Edge(14, 3));
    edges.push_back(Edge(21, 3));
    edges.push_back(Edge(26, 6));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(7, 8));
    edges.push_back(Edge(9, 8));
    edges.push_back(Edge(10, 3));
    edges.push_back(Edge(16, 6));
    edges.push_back(Edge(20, 3));
    edges.push_back(Edge(26, 4));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(5, 9));
    edges.push_back(Edge(11, 6));
    edges.push_back(Edge(16, 8));
    edges.push_back(Edge(23, 5));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(11, 3));
    edges.push_back(Edge(18, 3));
    edges.push_back(Edge(22, 5));
    edges.push_back(Edge(24, 2));
    edges.push_back(Edge(25, 6));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(0, 10));
    edges.push_back(Edge(8, 4));
    edges.push_back(Edge(13, 2));
    edges.push_back(Edge(23, 2));
    edges.push_back(Edge(25, 3));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(3, 4));
    edges.push_back(Edge(11, 3));
    edges.push_back(Edge(15, 6));
    edges.push_back(Edge(19, 1));
    edges.push_back(Edge(23, 6));
    edges.push_back(Edge(24, 3));
    edges.push_back(Edge(26, 7));
    adjList.push_back(edges);

    edges.clear();
    edges.push_back(Edge(4, 9));
    edges.push_back(Edge(20, 6));
    edges.push_back(Edge(21, 4));
    edges.push_back(Edge(25, 7));
    adjList.push_back(edges);
}