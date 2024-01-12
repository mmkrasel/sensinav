<?php
// Check if the constant INF is not already defined
if (!defined('INF')) {
    define('INF', 999999); // Represents infinity (large enough value to act as infinity)
}
###############################################################################
function init(){
    $data = array("nodes"=>array(), "edges"=>array());
    $building = "";
    $q = "SELECT * FROM inav_nodeinfo";
    if(isset($_REQUEST['building']) && !empty($_REQUEST['building'])){
        $building = @mysql_real_escape_string(trim($_REQUEST['building']));
        $q.= " WHERE building='$building'";
    }
    $r = @mysql_query($q);
    $nodes = array();
    if($r && mysql_num_rows($r)>0){
        while(($row=mysql_fetch_assoc($r))!=0){
            $data['nodes'][] = array('id'=>$row['id'], 'building'=>$row['building'], 'x'=>$row['x'], 'y'=>$row['y'], 'z'=>$row['z'], 'label'=>$row['label']);
            #$data['nodes'][] = array($row['id'], $row['x'], $row['y'], $row['z'], $row['label']);
            $nodes[] = $row['id'];
        }
    }
    $nodes = array_unique($nodes);
    $q = "SELECT * FROM inav_edgeinfo";
    $r = @mysql_query($q);
    if($r && mysql_num_rows($r)>0){
        while(($row=mysql_fetch_assoc($r))!=0){
            $data['edges'][] = array('node1'=>$row['node1'], 'node2'=>$row['node2'], 'distance'=>$row['distance']);
            #$data['edges'][] = array($row['node1'], $row['node2'], $row['distance']);
        }
    }
    return array("status"=>"OK", "data"=>$data);
}
###############################################################################
function sync_map($dbc){
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    #print_r($data);
    #var_dump($data);
    $nodes = $data['nodes'];
    $edges = $data['edges'];
    // echo "Here\n";
    // print_r($nodes);
    @mysqli_query($dbc, "TRUNCATE TABLE inav_nodeinfo");
    @mysqli_query($dbc, "TRUNCATE TABLE inav_edgeinfo");
    $p_id = -1;
    $p_label = "";
    $p_building = "";
    $p_z = -9999;
    $status = 0;
    foreach($nodes as $node){
        $id     = $node[0];
        $building  = $node[1];
        $label  = $node[2];
        $x      = $node[3];
        $y      = $node[4];
        $z      = $node[5];
        if($p_id==$id || ($building==$p_building && $label==$p_label && $p_z==$z)){
            continue;
        }
        $p_id = $id;
        $p_label = $label;
        $p_building = $building;
        $p_z = $z;
        $q = "INSERT INTO inav_nodeinfo(id,building,label,x,y,z) VALUES($id,'$building','$label',$x,$y,$z);";
        @mysqli_query($dbc, $q);
        $status++;
    }
    #echo "Here\n";
    #print_r($edges);
    $p_node1 = -9999;
    $p_node2 = -9999;
    foreach($edges as $edge){
        $node1      = $edge[0];
        $node2      = $edge[1];
        $distance   = $edge[2];
        if($p_node1==$node1 && $p_node2==$node2){
            #echo "$p_node1==$node1 && $p_node2==$node2\n";
            continue;
        }
        $p_node1 = $node1;
        $p_node2 = $node2;
        $q = "INSERT INTO inav_edgeinfo(node1,node2,distance)VALUES($node1,$node2,$distance);";
        @mysqli_query($dbc, $q);
        $status++;
    }
    return array("status"=>"OK", "data"=>$status);
}
###############################################################################
function create_node(){
    if(isset($_REQUEST['id'])&&isset($_REQUEST['building'])&&isset($_REQUEST['label'])&&isset($_REQUEST['x'])&&isset($_REQUEST['y'])&&isset($_REQUEST['z'])){
        $id = @mysql_real_escape_string(trim($_REQUEST['id']));
        $building  = @mysql_real_escape_string(trim($_REQUEST['building']));
        $label = @mysql_real_escape_string(trim($_REQUEST['label']));
        $x = @mysql_real_escape_string(trim($_REQUEST['x']));
        $y = @mysql_real_escape_string(trim($_REQUEST['y']));
        $z = @mysql_real_escape_string(trim($_REQUEST['z']));
        if($label!=""&&$x!=""&&$y!=""&&$x!=""){
            $q = "INSERT INTO inav_nodeinfo(id,building,label,x,y,z) VALUES($id,'$building',$label', '$x', '$y', '$z')";
            $r = @mysql_query($q);
            if($r){
                if(isset($_REQUEST['from_node'])&&isset($_REQUEST['distance'])){
                    $_REQUEST['to_node'] = $id;
                    return create_edge();
                }
                return array("status"=>"OK");
            }
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function create_edge(){
    if(isset($_REQUEST['from_node'])&&isset($_REQUEST['to_node'])&&isset($_REQUEST['distance'])){
        $from_node = @mysql_real_escape_string(trim($_REQUEST['from_node']));
        $to_node = @mysql_real_escape_string(trim($_REQUEST['to_node']));
        $distance = @mysql_real_escape_string(trim($_REQUEST['distance']));
        if($distance!=""&&$from_node!=""&&$to_node!=""){
            $q = "INSERT INTO inav_edgeinfo(node1,node2,distance) VALUES($fnode,$tnode,$distance)";
            $r = @mysql_query($q);
            if($r){
                return array("status"=>"OK");
            }
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function update_node(){
    if(isset($_REQUEST['id'])&&isset($_REQUEST['building'])&&isset($_REQUEST['label'])&&isset($_REQUEST['x'])&&isset($_REQUEST['y'])&&isset($_REQUEST['z'])){
        $id = @mysql_real_escape_string(trim($_REQUEST['id']));
        $building  = @mysql_real_escape_string(trim($_REQUEST['building']));
        $label = @mysql_real_escape_string(trim($_REQUEST['label']));
        $x = @mysql_real_escape_string(trim($_REQUEST['x']));
        $y = @mysql_real_escape_string(trim($_REQUEST['y']));
        $z = @mysql_real_escape_string(trim($_REQUEST['z']));
        if($id!=""&&$label!=""&&$x!=""&&$y!=""&&$x!=""){
            $q = "UPDATE inav_nodeinfo SET building='$building',label='$lable', x='$x', y='$y', z='$z' WHERE id=$id";
            $r = @mysql_query($q);
            if($r){
                update_edge($id, "", "");
                return array("status"=>"OK");
            }
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function update_edge($from_node, $to_node, $distance){
    if(isset($_REQUEST['from_node'])){
        $fn = @mysql_real_escape_string(trim($_REQUEST['from_node']));
        if($fn!=""){
            $from_node = $fn;
        }
    }
    if(isset($_REQUEST['to_node'])){
        $tn = @mysql_real_escape_string(trim($_REQUEST['to_node']));
        if($fn!=""){
            $to_node = $tn;
        }
    }
    if(isset($_REQUEST['distance'])){
        $distance = @mysql_real_escape_string(trim($_REQUEST['distance']));
    }
    if($distance!=""&&$from_node!=""&&$to_node!=""){
        $q = "UPDATE inav_edgeinfo SET distance=$distance WHERE (node1=$fnode AND node2=$tnode) OR (node2=$fnode AND node1=$tnode)";
        $r = @mysql_query($q);
        if($r){
            return array("status"=>"OK");
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function delete_node(){
    if(isset($_REQUEST['id'])){
        $id = @mysql_real_escape_string(trim($_REQUEST['id']));
        if($id!=""){
            $q = "DELETE FROM inav_nodeinfo WHERE id=$id";
            $r = @mysql_query($q);
            if($r){
                $q = "DELETE FROM inav_edgeinfo WHERE node1=$id OR node2=$$id";
                @mysql_query($q);
                return array("status"=>"OK");
            }
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function get_node_by_id(){
    if(isset($_REQUEST['id'])){
        $id = @mysql_real_escape_string(trim($_REQUEST['id']));
        if($id!=""){
            $q = "SELECT * FROM inav_nodeinfo id=$id";
            $r = @mysql_query($q);
            if($r && mysql_num_rows($r)>0){
                $row = mysql_fetch_assoc($r);
                $data = array('id'=>$row['id'], 'building'=>$row['building'], 'x'=>$row['x'], 'y'=>$row['y'], 'z'=>$row['z'], 'label'=>$row['label']);
                return array("status"=>"OK", "data"=>$data);
            }
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function get_shortest_path(){
    if(isset($_REQUEST['from_node'])&&isset($_REQUEST['to_node'])){
        $fn = @mysql_real_escape_string(trim($_REQUEST['from_node']));
        $tn = @mysql_real_escape_string(trim($_REQUEST['to_node']));
        if($fn!="" && $tn!=""){
            $q = "SELECT * FROM inav_shortpath WHERE from_node=$fn AND to_node=$tn";
            $r = @mysql_query($q);
            if($r && mysql_num_rows($r)>0){
                $row = mysql_fetch_assoc($r);
                $data = array('from_node'=>$row['from_node'], 'to_node'=>$row['to_node'], 'path'=>$row['path'], 'distance'=>$row['distance']);
                return array("status"=>"OK", "data"=>$data);
            }
        }
    }
    return array("status"=>"ERR");
}
###############################################################################
function floydWarshall(&$distance, &$next, $numVertices, $indexToNode) {
    // Apply Floyd-Warshall algorithm
    for ($k = 0; $k < $numVertices; ++$k) {
        for ($i = 0; $i < $numVertices; ++$i) {
            for ($j = 0; $j < $numVertices; ++$j) {
                if ($distance[$i][$k] != INF && $distance[$k][$j] != INF && $distance[$i][$k] + $distance[$k][$j] < $distance[$i][$j]) {
                    $distance[$i][$j] = $distance[$i][$k] + $distance[$k][$j];
                    $next[$i][$j] = $next[$i][$k];
                }
            }
        }
    }
    $paths = array();
    for ($i = 0; $i < $numVertices; ++$i) {
        for ($j = 0; $j < $numVertices; ++$j) {
            if ($i != $j) {
                $from = $indexToNode[$i];
                $to = $indexToNode[$j];

                $path = array($from);
                $intermediate = $i;

                // Use a while loop that terminates when it reaches the destination node ($j)
                while ($intermediate != $j && $intermediate < $numVertices) {
                    $intermediate = $next[$intermediate][$j];
                    $path[] = $indexToNode[$intermediate];
                }

                $paths[] = array(
                    "from" => $from,
                    "to" => $to,
                    "path" => implode(",",$path),
                    "distance" => $distance[$i][$j] == INF ? INF : $distance[$i][$j]
                );
            }
        }
    }
    // Unset variables to free up memory
    unset($distance);
    unset($next);
    unset($indexToNode);
    return $paths;
}
function trigger_shortest_paths(){
    @mysql_query("TRUNCATE table inav_shortpath");
    $nodes = array();
    $distances = array();
    $next = array();
    $q = "SELECT * FROM inav_edgeinfo";
    $r = @mysql_query($q);
    if($r && mysql_num_rows($r)){
        $data = array();
        while(($row=mysql_fetch_assoc($r))!=0){
            $data[] = array('node1'=>$row['node1'], 'node2'=>$row['node2'], 'distance'=>$row['distance']);
            $node1 = $row["node1"];
            $node2 = $row["node2"];
            $distance = $row["distance"];
            if (!in_array($node1, $nodes)) {
                $nodes[] = $node1;
            }
            if (!in_array($node2, $nodes)) {
                $nodes[] = $node2;
            }
            $node1Index = array_search($node1, $nodes);
            $node2Index = array_search($node2, $nodes);
            $distances[$node1Index][$node2Index] = $distance;
            $distances[$node2Index][$node1Index] = $distance; // Add the reverse edge for an undirected graph
        }
    }
    $numVertices = count($nodes);
    for ($i = 0; $i < $numVertices; ++$i) {
        for ($j = 0; $j < $numVertices; ++$j) {
            if (!isset($distances[$i][$j])) {
                $distances[$i][$j] = INF;
            }
            if ($i === $j) {
                $distances[$i][$j] = 0; // Distance to itself is 0
            }
            $next[$i][$j] = $j;
        }
    }
    #echo "<pre>"; print_r($nodes);
    #echo "<pre>"; print_r($numVertices);
    #echo "<pre>"; print_r($next);
    #echo "<pre>"; print_r($distances);
    
    
    $paths = floydWarshall($distances, $next, $numVertices, $nodes);
    $count = 0;
    $values = array();
    foreach($paths as $path){
        $count++;
        $f = $path['from'];
        $t = $path['to'];
        $p = $path['path'];
        $d = $path['distance'];
        $values[] = "($f, $t, '$p', $d)";
        if($count%100==0){
            $values = implode(",", $values);
            $q = "INSERT INTO inav_shortpath(from_node, to_node, path, distance) VALUES $values;";
            @mysql_query($q);
            $values = array();
        }
    }
    if($count%100 > 0){
        $values = implode(",", $values);
        $q = "INSERT INTO inav_shortpath(from_node, to_node, path, distance) VALUES $values;";
        @mysql_query($q);
        $values = array();
    }    
    return array("status"=>"OK");
}
###############################################################################
$response = array("status"=>"ERR");
if(isset($_REQUEST['action'])){

    #%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    #%%% Make your Database connection here %%%
    #%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    $dbc = connect_db();
    #%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
    $action = $_REQUEST['action'];
    if($action=="init"){
        $response = init();
    } else if($action=="node_create"){
        $response = create_node();
        // if($node_id > 0){
        //     $response = create_edge($node_id, "", "");
        // }
    } else if($action=="node_update"){
        $response = update_node();
    } else if($action=="node_delete"){
        $response = delete_node();
    } else if($action=="node_by_id"){
        $response = get_node_by_id();
    } else if($action=="edge_connect"){
        $response = create_edge();
    } else if($action=="shortest_path"){
        $response = get_shortest_path();
    } else if($action=="trigger_shortest_paths"){
        $response = trigger_shortest_paths();
    } else if($action=="sync_map"){
        $response = sync_map($dbc);
        trigger_shortest_paths();
    }
}
$json = json_encode($response,JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE);
echo $json;
?>